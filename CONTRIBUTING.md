# Contributing to ScaffoldKit 🏗️

Welcome! We are building this to be the standard for Spring AI introspection, and community input is highly encouraged.

### 💡 Ideas, Bugs, and Feature Requests

**Don't be shy!** If you have an idea for a new tool, see a way to improve the architecture, or found a bug, please **open an Issue** first. Anyone can open an issue. 

Discussing your idea in an issue before writing code ensures we are aligned on the "Spring Way" and saves everyone time!

### 🛡️ The Golden Rules for Code

1. **Never Push to `main`:** All changes must happen in a branch on your fork and be submitted via a Pull Request.
2. **The Bouncer Rule:** PRs will not be merged unless the GitHub Actions CI/CD pipeline passes (the Green Checkmark).
3. **Single Source of Truth:** All configuration properties must be driven by `@ConfigurationProperties` beans (like our `McpProperties` class). Do not use `env.getProperty()` with hard-coded magic numbers in your logic.

### ✍️ Commit Messages

Use the "Architecture First" style:
* `feat: add LogTailer safety checks`
* `fix: resolve metadata collision on port 9090`

### Example project

You can use the following project to test ScaffoldKit in action. <https://github.com/jukkamic/spring-ai-test>. It has ScaffoldKiit in Maven dependencies. You will need to ```mvn install``` ScaffoldKit project to your local repository in order to use it in the test project.

It is designed for use with Cline but any Agent will work because all you need to do is to configure the MCP server url to your agent.