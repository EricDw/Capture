# Prompt: AI Chat UI Evolution

Use this prompt to guide an AI agent in planning and designing a more useful, transparent, and robust Chat UI.

---

## The Request

"I want you to act as an expert Android UX/UI designer and Frontend Engineer. I need you to redesign my current AI Chat interface to be more 'useful' and 'informative' for an advanced user. 

### Core Objectives:
1. **Context Visibility**: The user should always know which model is active, how many tokens are in the current context window, and if the model is initialized.
2. **Operational Transparency**: When the AI uses tools (like MCP, Editor, or Search), the UI should reflect the *state* of that tool call (Pending, Working, Success, Error).
3. **Information Density without Clutter**: Handle 'Thinking' processes and large tool schemas/results using hierarchical disclosure (expand/collapse).
4. **Ergonomic Excellence**: The input field must handle IME (keyboard) transitions smoothly and support multi-line expansion without obscuring the chat history.

### Specific Instructions for Planning:
- **Analyze the Event System**: Look at `AiResponseEvent` and ensure the UI can react to every state change (Chunk, Complete, Error, Progress, Permission).
- **Design for Tool Calls**: Create a reusable 'Tool Badge' component that shows the tool name and an icon, with distinct colors for success and error states.
- **Implement 'Thinking' Blocks**: Design a specific container for the AI's internal reasoning (captured from the `thought` channel) that is collapsed by default.
- **Responsive Navigation**: If a Navigation Drawer is used, ensure it follows Material 3 guidelines (fixed width, proper surface colors, and rounded trailing edges).

### Constraints:
- Use **Jetpack Compose** (Material 3).
- Adhere to **M3 Design Guidelines**.
- Ensure the implementation is **Multiplatform-ready** where possible.
- Use **Throttling** for rapid stream updates (like thinking tokens) to prevent UI jank."
