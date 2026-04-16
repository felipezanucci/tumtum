'use client'

import {
  GradientText,
  StatDisplay,
  GlowCard,
  GradientBackground,
  ShapeMonogram,
  Divider,
  StoryCard,
  Button,
  Badge,
  Card,
} from '@/components/ui'
import { colors, gradientPairs } from '@/lib/design-system'

function ColorSwatch({ name, hex }: { name: string; hex: string }) {
  return (
    <div className="flex flex-col gap-2">
      <div
        className="w-full h-20 rounded-xl border border-tumtum-border"
        style={{ backgroundColor: hex }}
      />
      <span className="text-label-sm uppercase text-tumtum-text-muted">{name}</span>
      <span className="text-xs text-tumtum-text-muted font-mono">{hex}</span>
    </div>
  )
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section className="space-y-8">
      <div className="space-y-2">
        <h2 className="text-display-sm text-tumtum-text-primary">{title}</h2>
        <Divider variant="gradient" />
      </div>
      {children}
    </section>
  )
}

export default function DesignSystemPage() {
  return (
    <div className="min-h-screen bg-tumtum-dark text-tumtum-text-primary">
      {/* Hero */}
      <GradientBackground variant="night" noise className="py-20 px-6">
        <div className="max-w-5xl mx-auto space-y-6">
          <span className="text-label-lg uppercase text-wrapped-magenta">
            Design System
          </span>
          <h1 className="text-display-xl">
            <GradientText variant="sunset">Tumtum</GradientText>
          </h1>
          <p className="text-xl text-tumtum-text-muted max-w-2xl">
            Sistema visual inspirado no Spotify Wrapped 2022 — formas interlocking,
            gradientes vibrantes, tipografia bold, e animações com personalidade.
          </p>
        </div>
      </GradientBackground>

      <div className="max-w-5xl mx-auto px-6 py-16 space-y-24">

        {/* ─── Colors: Brand ─── */}
        <Section title="Cores — Brand">
          <div className="grid grid-cols-4 sm:grid-cols-8 gap-4">
            <ColorSwatch name="Red" hex={colors.brand.red} />
            <ColorSwatch name="Red 2°" hex={colors.brand.redSecondary} />
            <ColorSwatch name="Accent" hex={colors.brand.accent} />
            <ColorSwatch name="Dark" hex={colors.surface.base} />
            <ColorSwatch name="Surface" hex={colors.surface.elevated} />
            <ColorSwatch name="Border" hex={colors.surface.border} />
            <ColorSwatch name="Text" hex={colors.text.primary} />
            <ColorSwatch name="Muted" hex={colors.text.muted} />
          </div>
        </Section>

        {/* ─── Colors: Wrapped Palette ─── */}
        <Section title="Cores — Wrapped Palette">
          <p className="text-tumtum-text-muted">
            Paleta vibrante com cores que &quot;vibram&quot; quando justapostas —
            inspirada no conceito de auto-expressão do Wrapped 2022.
          </p>
          <div className="grid grid-cols-4 sm:grid-cols-8 gap-4">
            {Object.entries(colors.wrapped).map(([name, hex]) => (
              <ColorSwatch key={name} name={name} hex={hex} />
            ))}
          </div>
        </Section>

        {/* ─── Gradients ─── */}
        <Section title="Gradientes">
          <p className="text-tumtum-text-muted">
            Gradientes multi-stop que criam profundidade visual e energia.
            Cada gradiente combina cores contrastantes para o efeito de vibração cromática.
          </p>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {gradientPairs.map((g) => (
              <div key={g.name} className="space-y-2">
                <div className={`h-32 rounded-2xl ${g.css}`} />
                <span className="text-label-sm uppercase text-tumtum-text-muted">
                  {g.name}
                </span>
              </div>
            ))}
          </div>
        </Section>

        {/* ─── Typography ─── */}
        <Section title="Tipografia">
          <div className="space-y-8">
            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">
                Display XL — Hero headlines
              </span>
              <p className="text-display-xl">Sinta o evento</p>
            </div>
            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">
                Display LG — Section headers
              </span>
              <p className="text-display-lg">Seus momentos</p>
            </div>
            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">
                Display MD — Card titles
              </span>
              <p className="text-display-md">Pico de emoção</p>
            </div>
            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">
                Display SM — Subsections
              </span>
              <p className="text-display-sm">Frequência cardíaca</p>
            </div>
            <Divider />
            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">
                Stat Hero — Key metrics
              </span>
              <p className="text-stat-hero text-gradient-warm">187</p>
            </div>
            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">
                Stat LG — Secondary metrics
              </span>
              <p className="text-stat-lg text-gradient-cool">142</p>
            </div>
            <Divider />
            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">
                Label LG
              </span>
              <p className="text-label-lg uppercase text-tumtum-text-muted">
                Batimentos por minuto
              </p>
            </div>
            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">
                Label SM
              </span>
              <p className="text-label-sm uppercase text-tumtum-text-muted">
                Frequência máxima
              </p>
            </div>
            <Divider />
            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">
                Brand Mark
              </span>
              <p className="font-brand text-2xl tracking-[3px] text-tumtum-text-primary">
                TUMTUM
              </p>
            </div>
          </div>
        </Section>

        {/* ─── Gradient Text ─── */}
        <Section title="Texto com Gradiente">
          <div className="space-y-6">
            <GradientText variant="warm" as="h3" className="text-display-md">
              Warm — Coral → Magenta
            </GradientText>
            <GradientText variant="cool" as="h3" className="text-display-md">
              Cool — Purple → Cyan
            </GradientText>
            <GradientText variant="neon" as="h3" className="text-display-md">
              Neon — Cyan → Lime
            </GradientText>
            <GradientText variant="fire" as="h3" className="text-display-md">
              Fire — Yellow → Orange → Magenta
            </GradientText>
            <GradientText variant="sunset" as="h3" className="text-display-md">
              Sunset — Coral → Magenta → Purple
            </GradientText>
          </div>
        </Section>

        {/* ─── Stat Display ─── */}
        <Section title="Stat Display">
          <p className="text-tumtum-text-muted">
            Números gigantes e impactantes — a linguagem visual Wrapped para métricas.
            Os números dominam o espaço, com labels discretos.
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-12">
            <StatDisplay value="187" label="Pico máximo" unit="BPM" size="hero" gradient="warm" />
            <StatDisplay value="142" label="Média durante o show" unit="BPM" size="lg" gradient="cool" />
            <StatDisplay value="78" label="Sincronia com artista" unit="%" size="lg" gradient="neon" />
          </div>
        </Section>

        {/* ─── Shape Monograms ─── */}
        <Section title="Monogramas — Formas">
          <p className="text-tumtum-text-muted">
            Formas geométricas sobrepostas e interlocking — a assinatura visual do
            Wrapped 2022. Cada combinação de forma + gradiente cria uma identidade única.
          </p>
          <div className="space-y-8">
            <div className="flex flex-wrap gap-6 items-center">
              <ShapeMonogram shape="blob" gradient="warm" size="lg" animate />
              <ShapeMonogram shape="blobAlt" gradient="cool" size="lg" animate />
              <ShapeMonogram shape="spike" gradient="fire" size="lg" />
              <ShapeMonogram shape="diamond" gradient="neon" size="lg" animate />
              <ShapeMonogram shape="hexagon" gradient="sunset" size="lg" />
              <ShapeMonogram shape="squircle" gradient="aurora" size="lg" />
            </div>

            <div className="flex flex-wrap gap-4 items-end">
              <ShapeMonogram shape="blob" gradient="warm" size="sm" />
              <ShapeMonogram shape="blob" gradient="cool" size="md" />
              <ShapeMonogram shape="blob" gradient="neon" size="lg" />
              <ShapeMonogram shape="blob" gradient="fire" size="xl" />
            </div>

            {/* Overlapping composition */}
            <div className="relative h-64 flex items-center justify-center">
              <ShapeMonogram
                shape="blob"
                gradient="warm"
                size="xl"
                animate
                opacity={70}
                className="absolute left-1/4 -translate-x-1/2"
              />
              <ShapeMonogram
                shape="blobAlt"
                gradient="cool"
                size="xl"
                animate
                opacity={60}
                className="absolute left-1/2 -translate-x-1/2"
              />
              <ShapeMonogram
                shape="blob"
                gradient="neon"
                size="lg"
                animate
                opacity={50}
                className="absolute left-3/4 -translate-x-1/2"
              />
            </div>
          </div>
        </Section>

        {/* ─── Glow Cards ─── */}
        <Section title="Glow Cards">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <GlowCard glow="magenta" className="p-8">
              <span className="text-label-sm uppercase text-wrapped-magenta">Pico do Show</span>
              <p className="text-display-md mt-2">A Sky Full of Stars</p>
              <p className="text-tumtum-text-muted mt-2">
                Seu coração bateu mais forte nesse momento
              </p>
              <p className="text-stat-md text-gradient-warm mt-4">187 BPM</p>
            </GlowCard>

            <GlowCard glow="cyan" className="p-8">
              <span className="text-label-sm uppercase text-tumtum-accent">Sincronia</span>
              <p className="text-display-md mt-2">Você + Chris Martin</p>
              <p className="text-tumtum-text-muted mt-2">
                Seus batimentos ficaram sincronizados
              </p>
              <p className="text-stat-md text-gradient-cool mt-4">78%</p>
            </GlowCard>

            <GlowCard glow="purple" className="p-8">
              <span className="text-label-sm uppercase text-wrapped-purple">Duração</span>
              <p className="text-display-md mt-2">2h 34min</p>
              <p className="text-tumtum-text-muted mt-2">
                Tempo total de emoção no evento
              </p>
            </GlowCard>

            <GlowCard glow="red" className="p-8">
              <span className="text-label-sm uppercase text-tumtum-red">Recorde</span>
              <p className="text-display-md mt-2">Top 5%</p>
              <p className="text-tumtum-text-muted mt-2">
                Você foi um dos fãs mais emocionados
              </p>
            </GlowCard>
          </div>
        </Section>

        {/* ─── Story Cards ─── */}
        <Section title="Story Cards (9:16)">
          <p className="text-tumtum-text-muted">
            Cards no formato story — o formato viral para compartilhamento.
            Cada card usa uma combinação de gradiente e tipografia bold.
          </p>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            <StoryCard background="warm" className="max-w-[220px]">
              <StoryCard.Header>
                <span className="text-label-sm uppercase opacity-80">Seu pico</span>
              </StoryCard.Header>
              <StoryCard.Body>
                <p className="text-stat-lg text-white">187</p>
                <p className="text-label-lg uppercase opacity-80">BPM</p>
              </StoryCard.Body>
              <StoryCard.Footer>
                <p className="font-brand text-sm tracking-[3px] opacity-60">TUMTUM</p>
              </StoryCard.Footer>
            </StoryCard>

            <StoryCard background="cool" className="max-w-[220px]">
              <StoryCard.Header>
                <span className="text-label-sm uppercase opacity-80">Sincronia</span>
              </StoryCard.Header>
              <StoryCard.Body>
                <p className="text-stat-lg text-white">78%</p>
                <p className="text-label-lg uppercase opacity-80">Match</p>
              </StoryCard.Body>
              <StoryCard.Footer>
                <p className="font-brand text-sm tracking-[3px] opacity-60">TUMTUM</p>
              </StoryCard.Footer>
            </StoryCard>

            <StoryCard background="night" className="max-w-[220px]">
              <StoryCard.Header>
                <span className="text-label-sm uppercase opacity-80">Momento</span>
              </StoryCard.Header>
              <StoryCard.Body>
                <p className="text-display-sm text-white text-center">
                  A Sky Full of Stars
                </p>
              </StoryCard.Body>
              <StoryCard.Footer>
                <p className="font-brand text-sm tracking-[3px] opacity-60">TUMTUM</p>
              </StoryCard.Footer>
            </StoryCard>

            <StoryCard background="fire" className="max-w-[220px]">
              <StoryCard.Header>
                <span className="text-label-sm uppercase opacity-80">Recorde</span>
              </StoryCard.Header>
              <StoryCard.Body>
                <p className="text-display-sm text-white text-center">
                  Top 5%
                </p>
                <p className="text-sm opacity-70 mt-2 text-center">dos fãs mais emocionados</p>
              </StoryCard.Body>
              <StoryCard.Footer>
                <p className="font-brand text-sm tracking-[3px] opacity-60">TUMTUM</p>
              </StoryCard.Footer>
            </StoryCard>
          </div>
        </Section>

        {/* ─── Existing Components (Enhanced) ─── */}
        <Section title="Componentes Base">
          <div className="space-y-8">
            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">Buttons</span>
              <div className="flex flex-wrap gap-4">
                <Button variant="primary" size="lg">Compartilhar</Button>
                <Button variant="secondary" size="lg">Ver detalhes</Button>
                <Button variant="ghost" size="lg">Cancelar</Button>
                <Button variant="danger" size="lg">Excluir</Button>
              </div>
              <div className="flex flex-wrap gap-4">
                <Button variant="primary" size="md">Médio</Button>
                <Button variant="primary" size="sm">Pequeno</Button>
                <Button variant="primary" size="md" loading>Carregando</Button>
              </div>
            </div>

            <Divider />

            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">Badges</span>
              <div className="flex flex-wrap gap-3">
                <Badge variant="default">Default</Badge>
                <Badge variant="success">Conectado</Badge>
                <Badge variant="warning">Pendente</Badge>
                <Badge variant="danger">Erro</Badge>
                <Badge variant="accent">Novo</Badge>
              </div>
            </div>

            <Divider />

            <div className="space-y-4">
              <span className="text-label-sm uppercase text-tumtum-text-muted">Card</span>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Card hoverable>
                  <Card.Header>
                    <Card.Title>Coldplay — São Paulo</Card.Title>
                  </Card.Header>
                  <Card.Content>
                    <p className="text-tumtum-text-muted">
                      Allianz Parque • 15 Mar 2026
                    </p>
                  </Card.Content>
                </Card>
                <Card>
                  <Card.Header>
                    <Card.Title>Flamengo vs Palmeiras</Card.Title>
                  </Card.Header>
                  <Card.Content>
                    <p className="text-tumtum-text-muted">
                      Maracanã • 22 Abr 2026
                    </p>
                  </Card.Content>
                </Card>
              </div>
            </div>
          </div>
        </Section>

        {/* ─── Elevation ─── */}
        <Section title="Elevação & Superfícies">
          <div className="grid grid-cols-2 sm:grid-cols-5 gap-4">
            {Object.entries(colors.surface).map(([name, hex]) => (
              <div key={name} className="space-y-2">
                <div
                  className="h-24 rounded-xl border border-tumtum-border"
                  style={{ backgroundColor: hex }}
                />
                <span className="text-label-sm uppercase text-tumtum-text-muted">{name}</span>
                <span className="text-xs text-tumtum-text-muted font-mono">{hex}</span>
              </div>
            ))}
          </div>
        </Section>

        {/* ─── Animations ─── */}
        <Section title="Animações">
          <p className="text-tumtum-text-muted">
            Cada forma tem sua própria linguagem de movimento — blobs morpham organicamente,
            formas geométricas rotacionam suavemente. Gradientes pulsam com vida.
          </p>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-8">
            <div className="flex flex-col items-center gap-4">
              <div className="w-20 h-20 bg-wrapped-warm rounded-2xl animate-float" />
              <span className="text-label-sm uppercase text-tumtum-text-muted">Float</span>
            </div>
            <div className="flex flex-col items-center gap-4">
              <div className="w-20 h-20 bg-wrapped-cool rounded-full animate-pulse-glow" />
              <span className="text-label-sm uppercase text-tumtum-text-muted">Pulse Glow</span>
            </div>
            <div className="flex flex-col items-center gap-4">
              <ShapeMonogram shape="blob" gradient="neon" size="md" animate />
              <span className="text-label-sm uppercase text-tumtum-text-muted">Morph</span>
            </div>
            <div className="flex flex-col items-center gap-4">
              <div className="text-4xl animate-heartbeat">❤</div>
              <span className="text-label-sm uppercase text-tumtum-text-muted">Heartbeat</span>
            </div>
          </div>
        </Section>

        {/* ─── Glow Effects ─── */}
        <Section title="Efeitos de Glow">
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-8">
            <div className="flex flex-col items-center gap-4">
              <div className="w-20 h-20 bg-tumtum-red rounded-full glow-red" />
              <span className="text-label-sm uppercase text-tumtum-text-muted">Red</span>
            </div>
            <div className="flex flex-col items-center gap-4">
              <div className="w-20 h-20 bg-wrapped-magenta rounded-full glow-magenta" />
              <span className="text-label-sm uppercase text-tumtum-text-muted">Magenta</span>
            </div>
            <div className="flex flex-col items-center gap-4">
              <div className="w-20 h-20 bg-tumtum-accent rounded-full glow-cyan" />
              <span className="text-label-sm uppercase text-tumtum-text-muted">Cyan</span>
            </div>
            <div className="flex flex-col items-center gap-4">
              <div className="w-20 h-20 bg-wrapped-purple rounded-full glow-purple" />
              <span className="text-label-sm uppercase text-tumtum-text-muted">Purple</span>
            </div>
          </div>
        </Section>

        {/* ─── Dividers ─── */}
        <Section title="Divisores">
          <div className="space-y-8">
            <div className="space-y-2">
              <span className="text-label-sm uppercase text-tumtum-text-muted">Default</span>
              <Divider />
            </div>
            <div className="space-y-2">
              <span className="text-label-sm uppercase text-tumtum-text-muted">Gradient</span>
              <Divider variant="gradient" />
            </div>
          </div>
        </Section>

        {/* Footer */}
        <div className="text-center py-12 space-y-4">
          <p className="font-brand text-xl tracking-[3px]">TUMTUM</p>
          <p className="text-tumtum-text-muted text-sm">
            Design System v1.0 — Inspirado no Spotify Wrapped 2022
          </p>
        </div>
      </div>
    </div>
  )
}
