from sqlalchemy.ext.asyncio import AsyncSession, async_sessionmaker, create_async_engine
from sqlalchemy.orm import DeclarativeBase

from app.config import settings

# `DATABASE_SSL=true` makes asyncpg refuse a connection it cannot encrypt.
# Heart-rate series cross this link; off the private network they must not
# cross it in the clear (LGPD audit, C3).
engine = create_async_engine(
    settings.database_url,
    echo=False,
    connect_args={"ssl": "require"} if settings.database_ssl else {},
)
async_session = async_sessionmaker(engine, class_=AsyncSession, expire_on_commit=False)


class Base(DeclarativeBase):
    pass


async def get_db() -> AsyncSession:
    async with async_session() as session:
        try:
            yield session
            await session.commit()
        except Exception:
            await session.rollback()
            raise
