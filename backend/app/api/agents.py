"""API endpoints for Tumtum AI agents."""

import uuid

from fastapi import APIRouter, HTTPException

from app.schemas.agent import AgentQueryRequest, AgentQueryResponse, ToolCallInfo
from app.services.agents.genz_researcher import run_research_agent

router = APIRouter(prefix="/api/agents", tags=["agents"])

# In-memory conversation store (use Redis in production)
_conversations: dict[str, list[dict]] = {}


@router.post("/genz-researcher", response_model=AgentQueryResponse)
async def query_genz_researcher(request: AgentQueryRequest) -> AgentQueryResponse:
    """Query the Gen Z research agent about trends in entertainment and sports.

    The agent specializes in understanding Gen Z behavior in the Brazilian
    market, with focus on concerts, festivals, football, and social media
    sharing patterns.

    Supports multi-turn conversations via conversation_id.
    """
    # Load conversation history if continuing
    history: list[dict] | None = None
    if request.conversation_id and request.conversation_id in _conversations:
        history = _conversations[request.conversation_id]

    try:
        result = await run_research_agent(
            user_message=request.message,
            conversation_history=history,
        )
    except Exception as e:
        raise HTTPException(
            status_code=502,
            detail=f"Erro ao consultar o agente de pesquisa: {str(e)}",
        )

    # Store conversation for follow-ups
    conversation_id = request.conversation_id or str(uuid.uuid4())
    _conversations[conversation_id] = result["conversation"]

    return AgentQueryResponse(
        response=result["response"],
        tools_used=[ToolCallInfo(**tc) for tc in result["tools_used"]],
        conversation_id=conversation_id,
    )


@router.delete("/conversations/{conversation_id}")
async def delete_conversation(conversation_id: str) -> dict:
    """Delete a conversation from memory."""
    if conversation_id in _conversations:
        del _conversations[conversation_id]
    return {"status": "ok"}
