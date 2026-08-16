package com.dewildte.capture

import com.dewildte.capture.commands.*
import com.dewildte.capture.data.MessageRole
import com.dewildte.capture.events.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
class AiStateImplTest {

    @Test
    fun new_conversation_clicked_creates_conversation() {
        val appContext = AppContextImpl()
        val aiState = AiStateImpl()
        aiState.tell(SetContext(appContext))
        
        aiState.tell(NewConversationClicked)
        
        assertEquals(1, aiState.conversations.size)
        assertEquals("New Conversation", aiState.conversations[0].title)
        assertNotNull(aiState.currentConversation)
        assertEquals(aiState.conversations[0].id, aiState.currentConversation?.id)
    }

    @Test
    fun send_message_clicked_updates_conversation_and_triggers_send() {
        var sendTriggered = false
        val controller: (Any) -> Unit = { message ->
            if (message is SendAiMessage) {
                sendTriggered = true
            }
        }
        
        val appContext = AppContextImpl(controller = controller)
        appContext.isAiModelReady = true
        val aiState = AiStateImpl()
        aiState.tell(SetContext(appContext))
        
        aiState.tell(NewConversationClicked)
        aiState.tell(MessageInputChanged("Hello AI"))
        aiState.tell(SendMessageClicked)
        
        assertTrue(aiState.isGenerating)
        assertEquals(2, aiState.currentConversation?.messages?.size)
        assertEquals(MessageRole.USER, aiState.currentConversation?.messages?.get(0)?.role)
        assertEquals("Hello AI", aiState.currentConversation?.messages?.get(0)?.content)
        assertEquals(MessageRole.AI, aiState.currentConversation?.messages?.get(1)?.role)
        assertTrue(sendTriggered)
        assertEquals("", aiState.currentMessage)
        assertEquals("Hello AI", aiState.currentConversation?.title)
    }

    @Test
    fun ai_response_chunk_appends_to_last_message_but_does_not_save() {
        var saveTriggeredCount = 0
        val controller: (Any) -> Unit = { message ->
            if (message is SaveConversationToStorage) {
                saveTriggeredCount++
            }
        }
        val appContext = AppContextImpl(controller = controller)
        val aiState = AiStateImpl()
        aiState.tell(SetContext(appContext))
        
        appContext.isAiModelReady = true
        aiState.tell(NewConversationClicked)
        aiState.tell(MessageInputChanged("Hi"))
        aiState.tell(SendMessageClicked) // This triggers 1 save (for the user message)
        
        val initialSaveCount = saveTriggeredCount
        
        aiState.tell(AiResponseChunk("Part 1"))
        aiState.tell(AiResponseChunk(" Part 2"))
        
        assertEquals(initialSaveCount, saveTriggeredCount, "Chunks should not trigger intermediate saves")
        
        val lastMessage = aiState.currentConversation?.messages?.last()
        assertEquals("Part 1 Part 2", lastMessage?.content)
    }

    @Test
    fun ai_response_complete_triggers_save() {
        var saveTriggeredCount = 0
        val controller: (Any) -> Unit = { message ->
            if (message is SaveConversationToStorage) {
                saveTriggeredCount++
            }
        }
        val appContext = AppContextImpl(controller = controller)
        val aiState = AiStateImpl()
        aiState.tell(SetContext(appContext))
        
        appContext.isAiModelReady = true
        aiState.tell(NewConversationClicked)
        aiState.tell(MessageInputChanged("Hi"))
        aiState.tell(SendMessageClicked)
        
        val countAfterSend = saveTriggeredCount
        assertTrue(countAfterSend > 0)
        
        aiState.tell(AiResponseChunk("Done"))
        aiState.tell(AiResponseComplete)
        
        assertEquals(countAfterSend + 1, saveTriggeredCount, "Completion should trigger one final save")
        assertFalse(aiState.isGenerating)
    }

    @Test
    fun ai_response_error_triggers_save() {
        var saveTriggeredCount = 0
        val controller: (Any) -> Unit = { message ->
            if (message is SaveConversationToStorage) {
                saveTriggeredCount++
            }
        }
        val appContext = AppContextImpl(controller = controller)
        val aiState = AiStateImpl()
        aiState.tell(SetContext(appContext))
        
        appContext.isAiModelReady = true
        aiState.tell(NewConversationClicked)
        aiState.tell(MessageInputChanged("Hi"))
        aiState.tell(SendMessageClicked)
        
        val countAfterSend = saveTriggeredCount
        assertTrue(countAfterSend > 0)
        
        aiState.tell(AiResponseError(Throwable("Failure")))
        
        assertEquals(countAfterSend + 1, saveTriggeredCount, "Error should trigger one final save")
        assertFalse(aiState.isGenerating)
    }

    @Test
    fun stop_generating_updates_state() {
        var stopTriggered = false
        val controller: (Any) -> Unit = { message ->
            if (message is StopAiGeneration) {
                stopTriggered = true
            }
        }
        val appContext = AppContextImpl(controller = controller)
        val aiState = AiStateImpl(isGenerating = true)
        aiState.tell(SetContext(appContext))
        
        aiState.tell(StopGeneratingClicked)
        
        assertFalse(aiState.isGenerating)
        assertTrue(stopTriggered)
    }

    @Test
    fun delete_conversation_removes_from_list() {
        val appContext = AppContextImpl()
        val aiState = AiStateImpl()
        aiState.tell(SetContext(appContext))
        
        aiState.tell(NewConversationClicked)
        val conversationId = aiState.currentConversation!!.id
        
        aiState.tell(DeleteConversationClicked(conversationId))
        
        assertTrue(aiState.conversations.isEmpty())
        assertNull(aiState.currentConversation)
    }
}
