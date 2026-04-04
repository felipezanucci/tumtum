from pydantic import BaseModel, Field


class AgentQueryRequest(BaseModel):
    """Request to the Gen Z research agent."""

    message: str = Field(
        ...,
        min_length=1,
        max_length=2000,
        description="Pergunta ou tópico para o agente pesquisar",
        examples=[
            "Quais artistas estão bombando entre a Gen Z no Brasil agora?",
            "Como a Gen Z consome futebol? Eles assistem jogos inteiros?",
            "Que tipo de card compartilhável teria mais viralidade no TikTok?",
        ],
    )
    conversation_id: str | None = Field(
        default=None,
        description="ID da conversa para continuar um diálogo anterior",
    )


class ToolCallInfo(BaseModel):
    """Info about a tool the agent used during reasoning."""

    tool: str
    input: dict


class AgentQueryResponse(BaseModel):
    """Response from the Gen Z research agent."""

    response: str = Field(
        ...,
        description="Resposta do agente com insights e recomendações",
    )
    tools_used: list[ToolCallInfo] = Field(
        default_factory=list,
        description="Ferramentas que o agente usou para chegar na resposta",
    )
    conversation_id: str = Field(
        ...,
        description="ID da conversa para continuar o diálogo",
    )
