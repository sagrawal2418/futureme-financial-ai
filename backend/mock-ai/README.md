# Mock AI

The running MVP uses `shared/ai-assistant/MockAiAssistantService`.

The backend provider architecture now includes `MockLlmProvider` and an `AnthropicLlmProvider` request builder. Claude must receive immutable shared-engine output, include assumption and risk context, and never calculate balances, scores, probabilities, or projections.
