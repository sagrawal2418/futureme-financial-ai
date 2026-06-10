# Mock AI

The running MVP uses `shared/ai-assistant/MockAiAssistantService`.

A future backend implementation may expose the same assistant contract and delegate explanations to Azure OpenAI. It must ground prompts in immutable `ScenarioResult` values, include assumption/risk context, and never ask the model to calculate balances or projections.
