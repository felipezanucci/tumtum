Botão da TumTum: cor chapada, radius 12px, texto preto sobre rosa/amarelo (regra dura de contraste), sem sombra e sem animação de pulso.

```jsx
<Button variant="primary" size="lg" full>Escolher como compartilhar</Button>
<Button variant="flash">Encerrar a noite</Button>
<Button variant="dark">Permitir leitura</Button>
```

`dark` é o botão da "tela quieta" (permissão/privacidade — nunca rosa ali). `quiet` herda a cor do texto corrente e funciona em qualquer superfície. Hover em produção: troca de cor sólida 120–200ms (ex.: rosa→amarelo), nunca transform.
