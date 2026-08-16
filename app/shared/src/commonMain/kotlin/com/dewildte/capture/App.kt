package com.dewildte.capture

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.dewildte.capture.commands.ClearActivePermissionRequest
import com.dewildte.capture.commands.TransitionToState
import com.dewildte.capture.components.EditorBottomSheet
import com.dewildte.capture.components.McpToolPermissionDialog
import com.dewildte.capture.components.SearchTopAppBar
import com.dewildte.capture.content.ai.AiContent
import com.dewildte.capture.content.drawer.FileDrawerContent
import com.dewildte.capture.content.editor.EditorContent
import com.dewildte.capture.content.empty.EmptyContent
import com.dewildte.capture.content.files.FileListContent
import com.dewildte.capture.content.loading.LoadingContent
import com.dewildte.capture.content.settings.SettingsContent
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.AiTabClicked
import com.dewildte.capture.events.CreateFileRequested
import com.dewildte.capture.events.CreateFolderRequested
import com.dewildte.capture.events.DeleteNodeRequested
import com.dewildte.capture.events.EditorTabClicked
import com.dewildte.capture.events.FileInDrawerClicked
import com.dewildte.capture.events.NewFileClicked
import com.dewildte.capture.events.RefreshWorkspaceRequested
import com.dewildte.capture.events.RenameNodeRequested
import com.dewildte.capture.events.SelectWorkspaceFolderRequested
import com.dewildte.capture.events.SetDrawerOpen
import com.dewildte.capture.events.SettingsClicked
import com.dewildte.capture.events.SystemBackButtonClicked
import com.dewildte.capture.events.ToggleAiAssistant
import com.dewildte.capture.events.ToggleDrawerClicked
import com.dewildte.capture.utils.MapParameterProvider
import com.dewildte.capture.utils.samples.SampleSnippets
import com.dewildte.capture.utils.samples.SampleText
import com.dewildte.capture.utils.tellDebugLog

@Composable
fun App(
    appContext: AppContext = rememberAppContext(),
) {
  val state = appContext.state
  val windowAdaptiveInfo = currentWindowAdaptiveInfo()
  val isExpanded = windowAdaptiveInfo.windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

  LaunchedEffect(appContext.isDrawerOpen) {
    if (appContext.isDrawerOpen && drawerState.isClosed) {
      drawerState.open()
    } else if (!appContext.isDrawerOpen && drawerState.isOpen) {
      drawerState.close()
    }
  }

  LaunchedEffect(drawerState.currentValue) {
    if (drawerState.isClosed && appContext.isDrawerOpen) {
      appContext.tell(SetDrawerOpen(false))
    }
  }

  ModalNavigationDrawer(
      drawerState = drawerState,
      gesturesEnabled = !isExpanded,
      drawerContent = {
        if (!isExpanded) {
          ModalDrawerSheet(
              windowInsets = WindowInsets.safeDrawing,
          ) {
            FileDrawerContent(
                nodes = appContext.workspaceNodes,
                selectedFilePath = (state as? EditorState)?.textFile?.path,
                onFileSelected = { file ->
                  appContext.tell(FileInDrawerClicked(file))
                  appContext.tell(SetDrawerOpen(false))
                },
                expandedFolders = appContext.expandedFolders as MutableMap<String, Boolean>,
                onCreateFileRequested = { parent, name ->
                  appContext.tell(CreateFileRequested(parent, name))
                },
                onCreateFolderRequested = { parent, name ->
                  appContext.tell(CreateFolderRequested(parent, name))
                },
                onRenameRequested = { node, newName ->
                  appContext.tell(RenameNodeRequested(node, newName))
                },
                onDeleteRequested = { node -> appContext.tell(DeleteNodeRequested(node)) },
                onRefreshClicked = { appContext.tell(RefreshWorkspaceRequested) },
                isInitializing = appContext.isWorkspaceLoading,
                isFolderSelected = appContext.workspaceFolderUri != null,
                onSelectFolderClicked = { appContext.tell(SelectWorkspaceFolderRequested) },
                onCollapseClicked = { appContext.tell(SetDrawerOpen(false)) },
                modifier = Modifier.fillMaxHeight(),
            )
          }
        }
      }
  ) {
    Scaffold(
        topBar = {
          SearchTopAppBar(
              onMenuClick = { appContext.tell(ToggleDrawerClicked) },
              onAiClick = {
                appContext.tell(ToggleAiAssistant)
              },
              onSettingsClick = {
                appContext.tell(TransitionToState(appContext.settingsState!!))
                appContext.tell(SettingsClicked)
              },
              showBackButton = appContext.backNavigationEnabled,
              showNavActions = !isExpanded,
              showMenuIcon = !isExpanded,
              onBackClick = {
                appContext.tell(SystemBackButtonClicked)
              },
          )
        },
        floatingActionButton = {
          if (state is FileListState) {
            var showFabMenu by remember { mutableStateOf(false) }
            Box {
              LargeFloatingActionButton(
                  onClick = { 
                      appContext.tellDebugLog("App", "FAB Primary Action Triggered")
                      appContext.tell(NewFileClicked) 
                  },
                  modifier =
                      Modifier.combinedClickable(
                          onClick = { 
                              appContext.tellDebugLog("App", "FAB Clicked (Modifier)")
                              appContext.tell(NewFileClicked) 
                          },
                          onLongClick = { 
                              appContext.tellDebugLog("App", "FAB Long Clicked")
                              showFabMenu = true 
                          },
                      ),
              ) {
                Icon(Icons.Default.Add, contentDescription = "Actions")
              }
              DropdownMenu(
                  expanded = showFabMenu,
                  onDismissRequest = { showFabMenu = false },
              ) {
                DropdownMenuItem(
                    text = { Text("New Note") },
                    onClick = {
                      appContext.tell(NewFileClicked)
                      showFabMenu = false
                    },
                )
              }
            }
          }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
      val workspaceWidth by
          animateDpAsState(
              targetValue = if (appContext.isDrawerOpen && isExpanded) 300.dp else 0.dp,
              label = "WorkspaceExpansion",
          )

      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        // Navigation Rail (Expanded)
        if (isExpanded) {
          NavigationRail(
              header = {
                IconButton(onClick = { appContext.tell(ToggleDrawerClicked) }) {
                  Icon(Icons.Default.Menu, contentDescription = "Toggle Drawer")
                }
              },
              modifier = Modifier.fillMaxHeight(),
          ) {
            NavigationRailItem(
                selected = state is EditorState || state is FileListState,
                onClick = {
                  appContext.tell(EditorTabClicked)
                },
                icon = { Icon(Icons.Default.Edit, contentDescription = "Notes") },
                label = { Text("Notes") },
            )

            NavigationRailItem(
                selected = state is AiState,
                onClick = {
                  appContext.tell(ToggleAiAssistant)
                  appContext.tell(AiTabClicked)
                },
                icon = { Icon(Icons.Default.Bolt, contentDescription = "AI Assistant") },
                label = { Text("AI Assistant") },
            )

            NavigationRailItem(
                selected = state is SettingsState,
                onClick = {
                  appContext.tell(TransitionToState(appContext.settingsState!!))
                  appContext.tell(SettingsClicked)
                },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                label = { Text("Settings") },
            )
          }
          VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        // Workspace Drawer (Adaptive - Permanent in Expanded mode)
        if (isExpanded && workspaceWidth > 0.dp) {
          Box(modifier = Modifier.width(workspaceWidth).fillMaxHeight()) {
            FileDrawerContent(
                nodes = appContext.workspaceNodes,
                selectedFilePath = (state as? EditorState)?.textFile?.path,
                onFileSelected = { file -> appContext.tell(FileInDrawerClicked(file)) },
                expandedFolders = appContext.expandedFolders as MutableMap<String, Boolean>,
                onCreateFileRequested = { parent, name ->
                  appContext.tell(CreateFileRequested(parent, name))
                },
                onCreateFolderRequested = { parent, name ->
                  appContext.tell(CreateFolderRequested(parent, name))
                },
                onRenameRequested = { node, newName ->
                  appContext.tell(RenameNodeRequested(node, newName))
                },
                onDeleteRequested = { node -> appContext.tell(DeleteNodeRequested(node)) },
                onRefreshClicked = { appContext.tell(RefreshWorkspaceRequested) },
                isInitializing = appContext.isWorkspaceLoading,
                isFolderSelected = appContext.workspaceFolderUri != null,
                onSelectFolderClicked = { appContext.tell(SelectWorkspaceFolderRequested) },
                onCollapseClicked = { appContext.tell(ToggleDrawerClicked) },
                modifier = Modifier.fillMaxSize(),
            )
          }
          VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }

        // Main Content
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          when {
            state is SettingsState -> {
              SettingsContent(
                  state = state,
                  modifier = Modifier.fillMaxSize().imePadding(),
              )
            }
            state is AiState && !isExpanded -> {
              AiContent(
                  state = state,
                  modifier = Modifier.fillMaxSize().imePadding(),
              )
            }
            state is FileListState -> {
              FileListContent(
                  state = state,
                  modifier = Modifier.fillMaxSize()
              )
            }
            state is EditorState -> {
              EditorContent(
                  state = state,
                  modifier = Modifier.fillMaxSize().imePadding(),
              )
            }
            state is EmptyState -> {
              EmptyContent(
                  modifier = Modifier.fillMaxSize(),
                  onEvent = state::tell
              )
            }
            else -> {
              EditorContent(
                  state = appContext.editorState ?: EditorStateImpl(),
                  modifier = Modifier.fillMaxSize().imePadding(),
              )
            }
          }
        }

        // Supporting Pane (AI)
        if (isExpanded && appContext.isAiAssistantVisible) {
          VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          Box(modifier = Modifier.width(350.dp).fillMaxHeight()) {
            AiContent(
                state = appContext.aiState ?: AiStateImpl(),
                modifier = Modifier.fillMaxSize().imePadding(),
            )
          }
        }
      }
    }
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
          appContext.tell(ClearActivePermissionRequest)
        },
    )
  }

  // region Bottom Sheet
  if (state is EditorState) {
    EditorBottomSheet(state = state)
  }
  // endregion Bottom Sheet
}

class AppSettingsContextPreviewParameterProvider : PreviewParameterProvider<AppContext> {

  val settingsContext =
      AppContextImpl(
          showLoading = false,
          state = SettingsStateImpl(),
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
@Preview(name = "Phone Portrait", device = Devices.PHONE)
@Preview(name = "Phone Landscape", device = "spec:width=891dp,height=411dp,orientation=landscape,dpi=440")
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.TABLET)
@Preview(name = "Desktop", device = Devices.DESKTOP)
private fun AppAdaptivePreview() {
  val appContext =
      AppContextImpl(
          showLoading = false,
          state = EditorStateImpl(
              textFile = TextFile(
                  path = SampleText.textFileName,
                  contents = SampleText.loremIpsum
              )
          ),
      )
  App(appContext = appContext)
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
