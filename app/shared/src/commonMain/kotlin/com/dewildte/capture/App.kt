package com.dewildte.capture

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.dewildte.capture.components.*
import com.dewildte.capture.content.ai.AiContent
import com.dewildte.capture.content.drawer.FileDrawerContent
import com.dewildte.capture.content.editor.EditorContent
import com.dewildte.capture.content.loading.LoadingContent
import com.dewildte.capture.content.settings.SettingsContent
import com.dewildte.capture.data.TextFile
import com.dewildte.capture.events.*
import com.dewildte.capture.navigation.AppRoute
import com.dewildte.capture.utils.MapParameterProvider
import com.dewildte.capture.utils.samples.SampleSnippets
import com.dewildte.capture.utils.samples.SampleText

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
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
      (appContext as? MutableAppContext)?.isDrawerOpen = false
    }
  }

  // Cast for mutation
  val mutableBackStack = appContext.navBackStack as MutableList<AppRoute>

  LaunchedEffect(appContext.navBackStack.size) {
    (appContext as? MutableAppContext)?.backNavigationEnabled = appContext.navBackStack.size > 1
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
                  (appContext as? MutableAppContext)?.isDrawerOpen = false
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
                onCollapseClicked = { (appContext as? MutableAppContext)?.isDrawerOpen = false },
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
                if (mutableBackStack.contains(AppRoute.AiAssistant)) {
                  mutableBackStack.remove(AppRoute.AiAssistant)
                } else {
                  mutableBackStack.add(AppRoute.AiAssistant)
                }
              },
              onSettingsClick = {
                if (!appContext.navBackStack.contains(AppRoute.Settings)) {
                  mutableBackStack.add(AppRoute.Settings)
                }
                appContext.tell(SettingsClicked)
              },
              showBackButton = appContext.navBackStack.size > 1,
              showNavActions = !isExpanded,
              showMenuIcon = !isExpanded,
              onBackClick = {
                appContext.tell(SystemBackButtonClicked)
              },
          )
        },
        floatingActionButton = {
          if (state is EditorState) {
            var showFabMenu by remember { mutableStateOf(false) }
            Box {
              LargeFloatingActionButton(
                  onClick = { state.tell(NewNoteClicked) },
                  modifier =
                      Modifier.combinedClickable(
                          onClick = { state.tell(NewNoteClicked) },
                          onLongClick = { showFabMenu = true },
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
                      state.tell(NewNoteClicked)
                      showFabMenu = false
                    },
                )
                DropdownMenuItem(
                    text = { Text("Insert Snippet") },
                    onClick = {
                      state.tell(InsertSnippetClicked)
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
            val currentRoute = appContext.navBackStack.lastOrNull() ?: AppRoute.Editor

            NavigationRailItem(
                selected = currentRoute == AppRoute.Editor,
                onClick = {
                  mutableBackStack.clear()
                  mutableBackStack.add(AppRoute.Editor)
                  appContext.tell(EditorTabClicked)
                },
                icon = { Icon(Icons.Default.Edit, contentDescription = "Editor") },
                label = { Text("Editor") },
            )

            NavigationRailItem(
                selected = currentRoute == AppRoute.AiAssistant,
                onClick = {
                  if (mutableBackStack.contains(AppRoute.AiAssistant)) {
                    mutableBackStack.remove(AppRoute.AiAssistant)
                  } else {
                    mutableBackStack.add(AppRoute.AiAssistant)
                  }
                  appContext.tell(AiTabClicked)
                },
                icon = { Icon(Icons.Default.Bolt, contentDescription = "AI Assistant") },
                label = { Text("AI Assistant") },
            )

            NavigationRailItem(
                selected = currentRoute == AppRoute.Settings,
                onClick = {
                  if (!appContext.navBackStack.contains(AppRoute.Settings)) {
                    mutableBackStack.add(AppRoute.Settings)
                  }
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
          val currentRoute = appContext.navBackStack.lastOrNull() ?: AppRoute.Editor
          when {
            state is SettingsState || currentRoute == AppRoute.Settings -> {
              SettingsContent(
                  state = (appContext.state as? SettingsState) ?: SettingsStateImpl(),
                  modifier = Modifier.fillMaxSize().imePadding(),
              )
            }
            currentRoute == AppRoute.AiAssistant && !isExpanded -> {
              AiContent(
                  state = appContext.aiState ?: AiStateImpl(),
                  modifier = Modifier.fillMaxSize().imePadding(),
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
        if (isExpanded && appContext.navBackStack.contains(AppRoute.AiAssistant)) {
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
          (appContext as? MutableAppContext)?.activePermissionRequest = null
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
