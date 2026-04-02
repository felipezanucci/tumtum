'use client'

import { Component, type ReactNode } from 'react'
import { Button } from '@/components/ui'

interface Props {
  children: ReactNode
  fallback?: ReactNode
}

interface State {
  hasError: boolean
  error: Error | null
}

export default class ErrorBoundary extends Component<Props, State> {
  constructor(props: Props) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error }
  }

  render() {
    if (this.state.hasError) {
      if (this.props.fallback) return this.props.fallback

      return (
        <div className="flex min-h-[400px] items-center justify-center">
          <div className="text-center">
            <div className="mb-4 text-4xl">😵</div>
            <h2 className="text-xl font-bold text-tumtum-text-primary">Algo deu errado</h2>
            <p className="mt-2 text-sm text-tumtum-text-muted">
              Ocorreu um erro inesperado. Tente recarregar a página.
            </p>
            <Button
              className="mt-4"
              onClick={() => {
                this.setState({ hasError: false, error: null })
                window.location.reload()
              }}
            >
              Recarregar
            </Button>
          </div>
        </div>
      )
    }

    return this.props.children
  }
}
