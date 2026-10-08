# Forge Studio — Native Android Mobile Developer Workspace

Forge Studio is a native Android application built with Kotlin and Jetpack Compose, faithfully reproducing the **Forge Studio Mobile UX Blueprint**.

## Technology Stack

- **Framework**: Android Studio, Kotlin, Jetpack Compose, Material 3
- **Architecture**: MVVM with unidirectional data flow (MVI patterns)
- **State Management**: ViewModel, Kotlin Coroutines, StateFlow
- **Typography**: Bundled Google Fonts — *Inter* for sleek UI typography and *JetBrains Mono* for code, addresses, and terminal logs
- **Security**: Android KeyStore for encrypting SSH credentials, OpenSSH keys, GitHub tokens, and BYOK secrets (AES/GCM)
- **Local Persistence & Preferences**: DataStore, SharedPreferences, in-memory reactive state
- **Network & Sandboxing**: OkHttp, Retrofit, Sandboxed WebView for in-app HTML and URL previews
- **Color Palette**:
  - Slate 950 (`#020617`): Primary app background
  - Slate 900 (`#0F172A`): Card & surface background
  - Slate 800 (`#1E293B`): Component surfaces & chips
  - Slate 700 (`#334155`): Borders & structural dividers
  - Slate 400 (`#94A3B8`): Secondary text
  - Slate 100 (`#F1F5F9`): High-contrast primary text
  - Emerald 400 (`#4ADE80`): Primary accent, user message bubbles, active indicators
  - Amber 400 (`#FBBF24`): Activity & status alerts

---

## Key Features

1. **Main Workspace**:
   - Compact dark cockpit designed for modern mobile phones.
   - Top App Bar with Project Name, Git branch & sync indicator (`⎇ main · synced`/`local`), and context-aware action button (`◉ Preview` in Chat, `⌨ Terminal` in View Code).
   - Dynamic spring-animated footer toggle switching between **Chat** and **View Code**.

2. **Chat Mode & AI Coding Agent**:
   - Streaming agent responses supporting tool execution events, approvals, inline terminal cards, and file mutations.
   - User chat bubbles in Emerald (`#4ADE80`), agent bubbles in Slate 800.
   - Message Composer with Active Model chip, Active Agent chip, and `👁 Preview shared with agent` indicator tag.

3. **View Code Mode & Mobile Code Editor**:
   - Expandable project file tree with syntax icons (`◇` TypeScript/JS, `◈` HTML, `▫` config files).
   - Code Editor with line numbers gutter, syntax highlighting (keywords, strings, functions, comments), horizontal & vertical scrolling, search & replace, copy, undo, and redo.

4. **In-App Sandboxed Preview**:
   - Full-screen overlay supporting local HTML preview (rendered from `index.html`) or remote URLs.
   - `👁 Share` toggle to expose preview structure and status directly to the AI coding agent.

5. **Terminal Overlay with Aura AI**:
   - Full-screen terminal featuring `$ ` prompt, command execution (`git`, `docker-compose`, `npm`, `forge`, `ssh`, `cat`, `ls`, `pwd`), Aura intelligent suggestions (`Aura Flash`, `Aura Code`, `Aura Deep`), and tab completion (`⇥`).

6. **Navigation Drawer (Hub)**:
   - User profile header with plan status (`✦ One` / `Upgrade`).
   - Recent Projects horizontal carousel with gradient thumbnails, tool counts, and `+ New project` card.
   - Tools Deck for per-project SSH, GitHub auto-commit, and uVault synchronization.
   - BYOK inference configuration (DigitalOcean, Google Cloud, AWS Bedrock).
   - Usage meters for uVault disk space and Forge Co-Pilot tokens.
   - Access to Plugins and Tools managers.

7. **Security & Offline Durability**:
   - Android Keystore AES-GCM encryption for all sensitive secrets.
   - Completely functional offline demo mode seeded with `billing-api` and `auth-gateway`.
