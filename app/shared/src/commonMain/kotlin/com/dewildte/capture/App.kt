package com.dewildte.capture

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigationsuite.ExperimentalMaterial3AdaptiveNavigationSuiteApi
import androidx.compose.material3.adaptive.navigation3.SupportingPaneSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberSupportingPaneSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.entryProvider
import com.dewildte.capture.navigation.AppRoute
import com.dewildte.capture.commands.Start
import com.dewildte.capture.commands.TransitionToState
import com.dewildte.capture.components.AiTopBar
import com.dewildte.capture.components.EditorBottomSheet
import com.dewildte.capture.components.EditorTopBar
import com.dewildte.capture.components.McpToolPermissionDialog
import com.dewildte.capture.components.SettingsTopBar
import com.dewildte.capture.content.ai.AiContent
import com.dewildte.capture.content.drawer.FileDrawerContent
import com.dewildte.capture.content.editor.EditorContent
import com.dewildte.capture.content.empty.EmptyContentController
import com.dewildte.capture.content.loading.LoadingContent
import com.dewildte.capture.content.settings.SettingsContent
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.*
import com.dewildte.capture.utils.MapParameterProvider
import com.dewildte.capture.utils.samples.SampleSnippets
import com.dewildte.capture.utils.samples.SampleText

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3AdaptiveNavigationSuiteApi::class)
@Composable
fun App(
    appContext: AppContext = rememberAppContext(),
) {
  val state = appContext.state
  
  // Cast for mutation
  val mutableBackStack = appContext.navBackStack as MutableList<AppRoute>

  LaunchedEffect(appContext.navBackStack.size) {
      (appContext as? MutableAppContext)?.backNavigationEnabled = appContext.navBackStack.size > 1
  }

  val supportingPaneStrategy = rememberSupportingPaneSceneStrategy<AppRoute>(
      backNavigationBehavior = BackNavigationBehavior.PopUntilCurrentDestinationChange
  )

  NavigationSuiteScaffold(
      navigationSuiteItems = {
          item(
              selected = appContext.navBackStack.contains(AppRoute.Editor) && !appContext.navBackStack.contains(AppRoute.Settings),
              onClick = { 
                  mutableBackStack.clear()
                  mutableBackStack.add(AppRoute.Editor)
                  appContext.tell(EditorTabClicked)
              },
              icon = { Icon(Icons.Default.Edit, contentDescription = "Editor") },
              label = { Text("Editor") }
          )

          item(
              selected = appContext.navBackStack.contains(AppRoute.Settings),
              onClick = {
                  mutableBackStack.clear()
                  mutableBackStack.add(AppRoute.Settings)
                  appContext.tell(SettingsClicked)
              },
              icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
              label = { Text("Settings") }
          )

          item(
              selected = appContext.navBackStack.contains(AppRoute.AiAssistant),
              onClick = {
                  if (mutableBackStack.contains(AppRoute.AiAssistant)) {
                      mutableBackStack.remove(AppRoute.AiAssistant)
                  } else {
                      mutableBackStack.add(AppRoute.AiAssistant)
                  }
              },
              icon = { Icon(Icons.Default.Bolt, contentDescription = "AI Assistant") },
              label = { Text("AI Assistant") }
          )
          
          item(
              selected = appContext.isDrawerOpen,
              onClick = { appContext.tell(ToggleDrawerClicked) },
              icon = { Icon(Icons.Default.Menu, contentDescription = "Workspace") },
              label = { Text("Workspace") }
          )
      }
  ) {
      val workspaceWidth by animateDpAsState(
          targetValue = if (appContext.isDrawerOpen) 300.dp else 0.dp,
          label = "WorkspaceExpansion",
      )

      Row(modifier = Modifier.fillMaxSize()) {
          // Workspace Rail
          Box(modifier = Modifier.width(workspaceWidth).fillMaxHeight()) {
              FileDrawerContent(
                  nodes = appContext.workspaceNodes,
                  selectedFilePath = (state as? EditorState)?.textFile?.path,
                  onFileSelected = { file -> appContext.tell(FileInDrawerClicked(file)) },
                  expandedFolders = appContext.expandedFolders as MutableMap<String, Boolean>,
                  onCreateFileRequested = { parent, name -> appContext.tell(CreateFileRequested(parent, name)) },
                  onCreateFolderRequested = { parent, name -> appContext.tell(CreateFolderRequested(parent, name)) },
                  onRenameRequested = { node, newName -> appContext.tell(RenameNodeRequested(node, newName)) },
                  onDeleteRequested = { node -> appContext.tell(DeleteNodeRequested(node)) },
                  onRefreshClicked = { appContext.tell(RefreshWorkspaceRequested) },
                  isInitializing = appContext.isWorkspaceLoading,
                  isFolderSelected = appContext.workspaceFolderUri != null,
                  onSelectFolderClicked = { appContext.tell(SelectWorkspaceFolderRequested) },
                  onCollapseClicked = { appContext.tell(ToggleDrawerClicked) },
                  modifier = Modifier.fillMaxSize()
              )
          }

          if (workspaceWidth > 0.dp) {
              VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          }

          // Main Content with Supporting Pane
          NavDisplay(
              backStack = appContext.navBackStack,
              onBack = { if (mutableBackStack.isNotEmpty()) mutableBackStack.removeAt(mutableBackStack.size - 1) },
              sceneStrategies = listOf(supportingPaneStrategy),
              entryProvider = entryProvider {
                  addEntryProvider(
                      key = AppRoute.Editor,
                      metadata = SupportingPaneSceneStrategy.mainPane()
                  ) { _ ->
                      Scaffold(
                          topBar = { EditorTopBar(state = appContext.editorState ?: EditorStateImpl()) },
                          floatingActionButton = {
                              if (!appContext.navBackStack.contains(AppRoute.AiAssistant)) {
                                  FloatingActionButton(
                                      onClick = { mutableBackStack.add(AppRoute.AiAssistant) }
                                  ) {
                                      Icon(Icons.Default.Bolt, contentDescription = "AI Assistant")
                                  }
                              }
                          }
                      ) { innerPadding ->
                          EditorContent(
                              state = appContext.editorState ?: EditorStateImpl(),
                              modifier = Modifier
                                  .padding(innerPadding)
                                  .consumeWindowInsets(innerPadding)
                                  .imePadding(),
                          )
                      }
                  }

                  addEntryProvider(
                      key = AppRoute.Settings,
                      metadata = SupportingPaneSceneStrategy.mainPane()
                  ) { _ ->
                      Scaffold(
                          topBar = { SettingsTopBar(state = (appContext.state as? SettingsState) ?: SettingsStateImpl()) }
                      ) { innerPadding ->
                          SettingsContent(
                              state = (appContext.state as? SettingsState) ?: SettingsStateImpl(),
                              modifier = Modifier
                                  .padding(innerPadding)
                                  .consumeWindowInsets(innerPadding)
                                  .imePadding(),
                          )
                      }
                  }

                  addEntryProvider(
                      key = AppRoute.AiAssistant,
                      metadata = SupportingPaneSceneStrategy.supportingPane()
                  ) { _ ->
                      Scaffold(
                          topBar = { AiTopBar(state = appContext.aiState ?: AiStateImpl()) }
                      ) { innerPadding ->
                          AiContent(
                              state = appContext.aiState ?: AiStateImpl(),
                              modifier = Modifier
                                  .padding(innerPadding)
                                  .consumeWindowInsets(innerPadding)
                                  .imePadding(),
                          )
                      }
                  }
              }
          )
      }
      
      if (appContext.showLoading) {
          LoadingContent()
      }
      
      appContext.activePermissionRequest?.let { request ->
          McpToolPermissionDialog(
              toolName = request.toolName,
              arguments = request.arguments,
              onResult = { result ->
                  request.result.complete(result)
                  (appContext as? MutableAppContext)?.activePermissionRequest = null
              }
          )
      }

      // region Bottom Sheet
      if (state is EditorState) {
          EditorBottomSheet(state = state)
      }
      // endregion Bottom Sheet
  }
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
