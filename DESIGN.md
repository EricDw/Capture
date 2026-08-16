# Design Principles & Guidelines

This project strictly adheres to **Material 3 (M3)** design principles, specifically leveraging the **Expressive** design language to create a modern, dynamic, and adaptive user interface.

## Core Directives

1.  **Material 3 Expressive**: Always use Material 3 components and styling. Aim for an expressive UI that utilizes M3's advanced motion, color, and shape systems.
2.  **Standard Components First**: When building new UI or updating existing screens, you **MUST prefer using prebuilt Material 3 components** (e.g., `Scaffold`, `ModalNavigationDrawer`, `NavigationRail`, `Button`, `Card`, etc.). 
3.  **Custom Components**: Only build custom UI components if a standard M3 component cannot fulfill the requirement. If a custom component is necessary, it must still follow M3 design tokens (color, typography, elevation).
4.  **Stay Current**: Before implementing a design-heavy feature or complex M3 component, use your tools (like `web_search` or `search_android_docs`) to look up the **latest official documentation** and best practices for that component to ensure we are using the most up-to-date APIs and patterns.
5.  **Design Approval**: If a UI change significantly alters existing layouts or introduces new user flows, you MUST present a description (and a `render_compose_preview` if possible) to the user for approval before modifying code.

## Adaptive Design
UI must be adaptive. Use Material 3's window size classes to ensure the layout transitions gracefully between Phone, Tablet, and Desktop form factors.
