package com.dewildte.capture

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.dewildte.capture.commands.Start
import com.dewildte.capture.components.EditorBottomSheet
import com.dewildte.capture.components.EditorTopBar
import com.dewildte.capture.components.SettingsTopBar
import com.dewildte.capture.content.editor.EditorContent
import com.dewildte.capture.content.empty.EmptyContentController
import com.dewildte.capture.content.loading.LoadingContent
import com.dewildte.capture.content.settings.SettingsContent
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.utils.MapParameterProvider
import com.dewildte.capture.utils.samples.SampleSnippets
import com.dewildte.capture.utils.samples.SampleText

@Composable
fun App(
    appContext: AppContext = rememberAppContext(),
) {

  val state = appContext.state

  Scaffold(
      modifier = Modifier.fillMaxSize(),
      topBar = {
        when (state) {
          is EditorState -> {
            EditorTopBar(state = state)
          }

          is EmptyState -> {
            // TODO: Implement
          }

          is InitialState -> {
            // TODO: Implement
          }

          is SettingsState -> {
            SettingsTopBar(state = state)
          }
        }
      },
      bottomBar = {
        NavigationBar {
          NavigationBarItem(
              selected = false,
              onClick = {
                // TODO: Open a file picker
              },
              icon = {
                Icon(Icons.Default.Menu, null)
              },
          )

          NavigationBarItem(
              selected = false,
              onClick = {
                // TODO: Open the Editor
              },
              icon = {
                Icon(Icons.Default.Edit, null)
              },
          )

          NavigationBarItem(
              selected = false,
              onClick = {
                // TODO: Open the AI
              },
              icon = {
                Icon(Icons.Default.Bolt, null)
              },
          )
        }
      },
  ) { innerPadding ->
    when (state) {
      is EditorState -> {
        EditorContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
        )
      }

      is EmptyState -> {
        EmptyContentController(state = state)
      }

      is InitialState -> {
        appContext.tell(Start)
      }

      is SettingsState -> {
        SettingsContent(
            state = state,
            modifier = Modifier.padding(innerPadding),
        )
      }
    }

    if (appContext.showLoading) {
      LoadingContent()
    }
  }

  // region Bottom Sheet
  when (state) {
    is EditorState -> {
      EditorBottomSheet(state = state)
    }

    else -> {
      /* no-op */
    }
  }
  // endregion Bottom Sheet
}

class AppSettingsContextPreviewParameterProvider : PreviewParameterProvider<AppContext> {

  val settingsContext =
      AppContextImpl(
          showLoading = false,
          state =
              SettingsStateImpl(
                  snippets = SampleSnippets.basic10,
              ),
      )

  override val values: Sequence<AppContext>
    get() =
        sequenceOf(
            settingsContext,
        )
}

class AppEditorContextPreviewParameterProvider : MapParameterProvider<AppContext>() {

  override val valueMap: Map<String, AppContext> =
      mapOf(
          "With Content" to
              AppContextImpl(
                  showLoading = false,
                  state =
                      EditorStateImpl(
                          textFile =
                              TextFile(
                                  path = SampleText.textFileName,
                                  contents = SampleText.loremIpsum,
                              )
                      ),
              ),
          "Menu Open" to
              AppContextImpl(
                  showLoading = false,
                  state =
                      EditorStateImpl(
                          textFile =
                              TextFile(
                                  path = SampleText.textFileName,
                                  contents = SampleText.loremIpsum,
                              ),
                          moreMenuExpanded = true,
                      ),
              ),
          "Snippet Selector" to
              AppContextImpl(
                  showLoading = false,
                  state =
                      EditorStateImpl(
                          textFile =
                              TextFile(
                                  path = SampleText.textFileName,
                                  contents = SampleText.loremIpsum,
                              ),
                          snippetSelectorExpanded = true,
                          snippets = SampleSnippets.basic10,
                      ),
              ),
      )
}

@Composable
@Preview
private fun LoadingPreview() {
  App(appContext = AppContextImpl())
}

@Composable
@Preview
private fun AppEmptyPreview() {
  val appContext =
      AppContextImpl(
          showLoading = false,
          state = EmptyStateImpl(),
      )
  App(appContext = appContext)
}

@Composable
@Preview
private fun AppEditorPreview(
    @PreviewParameter(AppEditorContextPreviewParameterProvider::class) appContext: AppContext
) {
  App(appContext = appContext)
}

@Composable
@Preview
private fun AppSettingsPreview(
    @PreviewParameter(AppSettingsContextPreviewParameterProvider::class) appContext: AppContext,
) {
  App(appContext = appContext)
}
