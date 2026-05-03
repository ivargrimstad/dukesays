# Chaplin — The Know-Me Movie Engine

A personalized movie recommendation system built for the Jakarta EE Hackathon (ShiftAPPens 2026). Chaplin is an AI-powered film critic that connects to your TMDB account, learns your taste, and provides hallucination-free, context-aware movie guidance.

## Features

- TMDB OAuth integration — syncs your rated movies, favorites, and watchlist
- Persistent taste profile — favorite genres, top directors, preferred decades, average rating
- AI agent (Chaplin) with old-fashioned 1920s persona and movie-only guardrails
- Rate, favorite, and watchlist movies through natural conversation (syncs to both DB and TMDB)
- "I'm feeling lucky" — random obscure movie suggestions
- "Poorly explain a movie" — hilariously bad but accurate plot summaries
- Background sync every 5 minutes to catch changes made directly on TMDB
- Per-user chat memory for isolated conversations

## Tech Stack

- **Runtime:** WildFly 39.0.1.Final
- **AI:** LangChain4j-CDI 1.2.0-Beta1 + OpenAI gpt-4o-mini
- **Jakarta Specs:** CDI, REST, Persistence, JSON-P, Transactions, EJB, Concurrency, Servlet

## Requirements

- Java 25 (adoptium.net/marketplace)
- Maven 3.9.x
- OpenAI API key
- TMDB API key (included in source for hackathon)

## Quick Start

### 1. Clone and Build

```bash
git clone <repository-url>
cd jakartaee-knowme-challenge
mvn clean package
```

### 2. Set Environment Variables

Use the following commands depending on your operating system:

#### macOS and Linux

```bash
export OPENAI_API_KEY=your-openai-key
export TMDB_API_KEY=your-tmdb-api-key
```

#### Windows (Command Prompt)

```cmd
set OPENAI_API_KEY=your-openai-key
set TMDB_API_KEY=your-tmdb-api-key
```


### 3. Run on WildFly

```bash
mvn clean package wildfly:run
```

### 4. Open the App

Navigate to: `http://localhost:8080/duke-knows-me/index.html`

1. Click **Connect to Chaplin**
2. Approve the request on the TMDB tab that opens
3. Come back and click **I have approved it — continue**
4. Chat with Chaplin

### Other Servers

#### GlassFish
```bash
mvn clean package cargo:run -Pglassfish
```

#### Payara
```bash
mvn clean package cargo:run -Ppayara
```

#### Open Liberty
```bash
mvn clean package liberty:run
```

### Status Endpoint

`http://localhost:8080/duke-knows-me/status`
