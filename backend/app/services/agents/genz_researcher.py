"""
Gen Z Trends Research Agent for Tumtum.

This agent specializes in understanding Gen Z behavior, preferences,
and trends in entertainment (concerts, festivals, music) and sports
(especially football in Brazil). It helps the Tumtum team make
data-informed product decisions.
"""

from anthropic import AsyncAnthropic

from app.config import settings
from app.services.agents.prompts import GENZ_RESEARCHER_SYSTEM_PROMPT

client = AsyncAnthropic(api_key=settings.anthropic_api_key)

TOOLS = [
    {
        "name": "search_trends",
        "description": (
            "Search for current Gen Z trends in entertainment and sports. "
            "Use this to find recent data, viral moments, and cultural shifts."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "query": {
                    "type": "string",
                    "description": "The search query about Gen Z trends",
                },
                "category": {
                    "type": "string",
                    "enum": ["entertainment", "sports", "music", "festivals", "social_media", "general"],
                    "description": "Category to focus the search on",
                },
            },
            "required": ["query"],
        },
    },
    {
        "name": "analyze_event_appeal",
        "description": (
            "Analyze how appealing a specific event, artist, or sport is "
            "to Gen Z audiences in Brazil. Returns insights about engagement "
            "potential and shareability."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "subject": {
                    "type": "string",
                    "description": "The event, artist, team, or sport to analyze",
                },
                "context": {
                    "type": "string",
                    "description": "Additional context (e.g., 'upcoming tour in São Paulo')",
                },
            },
            "required": ["subject"],
        },
    },
    {
        "name": "generate_content_suggestions",
        "description": (
            "Generate suggestions for shareable content formats that resonate "
            "with Gen Z. Considers current social media trends and Tumtum's "
            "heart rate + emotion angle."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "event_type": {
                    "type": "string",
                    "enum": ["concert", "sports", "festival"],
                    "description": "Type of event to generate content ideas for",
                },
                "platform": {
                    "type": "string",
                    "enum": ["instagram", "tiktok", "x", "whatsapp"],
                    "description": "Target social media platform",
                },
            },
            "required": ["event_type"],
        },
    },
]


def _process_tool_call(tool_name: str, tool_input: dict) -> str:
    """Process tool calls from the agent and return simulated results.

    In production, these would call real APIs (Google Trends, social media
    APIs, etc.). For now, the agent uses its training knowledge to provide
    informed responses, and the tools serve as structured reasoning anchors.
    """
    if tool_name == "search_trends":
        query = tool_input.get("query", "")
        category = tool_input.get("category", "general")
        return (
            f"[Trend Research] Query: '{query}' | Category: {category}\n"
            f"Note: Use your extensive knowledge about Gen Z trends, "
            f"Brazilian entertainment culture, and social media behavior "
            f"to provide data-informed insights. Consider trends from "
            f"TikTok, Instagram Reels, Twitter/X, and Brazilian pop culture. "
            f"Focus on actionable insights for Tumtum's product."
        )

    if tool_name == "analyze_event_appeal":
        subject = tool_input.get("subject", "")
        context = tool_input.get("context", "")
        return (
            f"[Event Appeal Analysis] Subject: '{subject}' | Context: '{context}'\n"
            f"Analyze this from a Gen Z perspective considering: "
            f"social media virality potential, emotional intensity, "
            f"shareability of heart rate data, and relevance in Brazil. "
            f"Rate the appeal and explain why."
        )

    if tool_name == "generate_content_suggestions":
        event_type = tool_input.get("event_type", "")
        platform = tool_input.get("platform", "all")
        return (
            f"[Content Suggestions] Event type: '{event_type}' | Platform: '{platform}'\n"
            f"Generate content ideas that combine Tumtum's HR visualization "
            f"with current Gen Z content formats. Think about: carousel posts, "
            f"short-form video, interactive stories, comparison cards, and "
            f"viral hooks that drive organic sharing."
        )

    return f"[Unknown tool: {tool_name}]"


async def run_research_agent(
    user_message: str,
    conversation_history: list[dict] | None = None,
) -> dict:
    """Run the Gen Z research agent with a user query.

    Args:
        user_message: The research question or topic to explore.
        conversation_history: Optional prior messages for multi-turn conversations.

    Returns:
        Dict with 'response' (text), 'tool_calls' (list of tools used),
        and 'conversation' (full message history for follow-ups).
    """
    messages = list(conversation_history or [])
    messages.append({"role": "user", "content": user_message})

    tool_calls_made: list[dict] = []

    response = await client.messages.create(
        model="claude-sonnet-4-20250514",
        max_tokens=4096,
        system=GENZ_RESEARCHER_SYSTEM_PROMPT,
        tools=TOOLS,
        messages=messages,
    )

    # Agentic loop: keep processing tool calls until the model stops
    while response.stop_reason == "tool_use":
        tool_use_block = next(
            block for block in response.content if block.type == "tool_use"
        )

        tool_calls_made.append({
            "tool": tool_use_block.name,
            "input": tool_use_block.input,
        })

        tool_result = _process_tool_call(tool_use_block.name, tool_use_block.input)

        # Append assistant response + tool result
        messages.append({"role": "assistant", "content": response.content})
        messages.append({
            "role": "user",
            "content": [
                {
                    "type": "tool_result",
                    "tool_use_id": tool_use_block.id,
                    "content": tool_result,
                }
            ],
        })

        response = await client.messages.create(
            model="claude-sonnet-4-20250514",
            max_tokens=4096,
            system=GENZ_RESEARCHER_SYSTEM_PROMPT,
            tools=TOOLS,
            messages=messages,
        )

    # Extract final text response
    final_text = ""
    for block in response.content:
        if hasattr(block, "text"):
            final_text += block.text

    # Build final conversation for multi-turn
    messages.append({"role": "assistant", "content": response.content})

    return {
        "response": final_text,
        "tools_used": tool_calls_made,
        "conversation": messages,
    }
