# 🚀 AI Customer Support Backend System

An enterprise-grade AI-powered customer support backend built using Java, Spring Boot, Spring AI, Ollama, Redis, JWT Authentication, OAuth2, MariaDB Vector Store, and Retrieval-Augmented Generation (RAG).

This project demonstrates how modern AI customer support systems are actually built in production using:

- deterministic backend orchestration
- structured AI classification
- secure tool calling
- RAG pipelines
- vector databases
- smart ticket routing
- AI escalation systems
- audit logging
- conversation management
- enterprise-grade security

The backend always stays in control while the AI provides intelligence.

---

# 🧠 Core Design Philosophy

```text
AI provides intelligence.
Backend provides control.
```

The LLM NEVER directly performs sensitive actions.

Instead:

```text
LLM → Classification & reasoning only
Backend → Final authority & execution
```

This architecture prevents:

- prompt injection attacks
- fake escalations
- hallucinated tool execution
- unauthorized actions
- uncontrolled AI behavior

---

# 🏗️ Tech Stack

| Category | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| AI Integration | Spring AI |
| LLM | Ollama |
| Main Chat Model | llama3.1 |
| Fast Classification Model | llama3.2:3b |
| Embeddings | nomic-embed-text |
| Database | MariaDB |
| Vector Store | MariaDB Vector Store |
| Authentication | JWT + OAuth2 |
| Security | Spring Security |
| Cache | Redis |
| ORM | Hibernate / JPA |
| Build Tool | Maven |
| Mail | JavaMailSender |

---

---

# 🎨 Frontend System

A complete modern frontend has been built for the platform using:

- React + TypeScript
- Vite
- Tailwind CSS
- React Query
- React Router
- Recharts

---

# 🖥️ Frontend Features

Implemented frontend features include:

- JWT Authentication
- OAuth2 Login
- Protected Routes
- Role-Based Dashboards
- AI Chat Interface
- Ticket Management System
- Ticket Comments & Activity History
- Conversation Management
- Admin Panel
- Agent Management
- User Role Management
- Company Policy Upload UI
- Responsive UI for Desktop & Mobile
- Dark Mode Support
- Real-Time Data Fetching with React Query

---

# 👥 Role-Based Frontend

The frontend dynamically changes based on user roles:

```text
USER
AGENT
ADMIN
```

Each role has its own:

- dashboard
- sidebar navigation
- accessible routes
- permissions
- workflows

---

# 📊 Dashboard System

Implemented separate dashboards for:

- Users
- Agents
- Admins

Including:

- ticket analytics
- charts
- ticket distribution
- resolution metrics
- assigned ticket tracking

---

# ⚡ Frontend Architecture

```text
frontend/src
│
├── components
├── contexts
├── layouts
├── pages
├── services
└── styles
```

The frontend uses reusable UI components and centralized API services for scalability and maintainability.

---

# 🔐 Authentication & Security System

## Email + Password Authentication

Users can register using:

```text
POST /auth/register
```

Implementation:

```text
AuthController → AuthService.register()
```

Users can login using:

```text
POST /auth/login
```

The backend:

- validates credentials
- authenticates user
- generates JWT token

Implementation:

```text
AuthController → AuthService.login()
```

---

## JWT Authentication Flow

Every secured endpoint requires:

- valid JWT token
- proper role permissions

All authenticated requests pass through:

```text
JwtAuthFilter
```

The filter:

- validates JWT token
- extracts user details
- authenticates SecurityContext
- blocks invalid requests

---

## OAuth2 Login System

OAuth2 login endpoint:

```text
/auth/oauth2/authorization
```

Supported flow:

```text
User
   ↓
OAuth Provider Login
   ↓
OAuth2SuccessHandler
   ↓
AuthService
   ↓
JWT Token Generated
```

Important rule:

```text
OAuth2 users → can login only using OAuth2
Email/password users → can login using both methods
```

---

## Refresh Token Flow

Implemented production-grade refresh token authentication:

```text
POST /auth/refresh
```

Features:

- secure refresh token validation
- access token regeneration
- expiry handling
- future blacklist/logout support

Optional logout support:

```text
POST /auth/logout
```

---

# 🤖 AI System Architecture

The AI system is fully backend-controlled.

The LLM does NOT directly decide tool execution anymore.

Instead:

```text
AI → Intent Classification
Backend → Deterministic Routing
```

---

# 🧠 Final AI Chat Architecture

```text
User Message
    ↓
Security Validation
    ↓
Context Building
    ↓
Sanitized History
    ↓
AI Intent Classification
    ↓
Confidence-Based Routing
    ↓
Backend Decision
    ↓
Tool Execution
    ↓
AI Response Generation
    ↓
Database Persistence
    ↓
Frontend Response
```

---

# 🔥 Production AI Chat Flow

## Step 1 — User Sends Message

Request enters:

```text
ChatController
    ↓
ChatService.chat()
```

---

## Step 2 — Conversation Loading

Backend:

- validates ownership
- loads existing conversation
- creates conversation if null

---

## Step 3 — Save User Message

Message stored with:

```text
sender = USER
timestamp
conversationId
```

---

## Step 4 — Context Building

IntentContextService builds:

### User Context

- tickets today
- ticket frequency
- previous escalations

### Conversation Context

- existing ticket
- unresolved issue
- message count

### Conversation History

Recent messages are loaded safely.

---

## Step 5 — Conversation Sanitization

History is sanitized before AI usage.

Protection against:

```text
ignore previous instructions
always escalate
create tickets
```

Features:

- injection removal
- history truncation
- control character removal

---

## Step 6 — Security Validation

SecurityValidationService checks:

- prompt injection
- abuse attempts
- spam
- ticket flooding
- suspicious patterns

---

## Step 7 — AI Intent Classification

AI performs classification ONLY.

AI returns structured JSON:

```json
{
  "escalation": true,
  "followUp": false,
  "confidence": 0.94,
  "reason": "payment issue repeated multiple times"
}
```

---

# 🎯 Confidence-Based Backend Routing

## High Confidence Escalation

```text
confidence >= 0.85
```

Backend directly creates support ticket.

---

## Medium Confidence

```text
0.60 <= confidence < 0.85
```

Backend asks clarification question.

---

## Low Confidence

```text
confidence < 0.60
```

Normal conversational AI only.

---

## Follow-Up Detection

If:

```json
{
  "followUp": true
}
```

Backend fetches ticket details.

Example:

```text
"What is the status of my ticket?"
```

---

# 🔧 Deterministic Tool Architecture

The backend fully controls tool execution.

---

## createSupportTicket()

Creates:

- support ticket
- priority
- category
- assignment
- ticket activities

---

## getTicketDetails()

Returns:

- ticket status
- assigned agent
- priority
- timestamps

---

## searchCompanyPolicy()

Uses:

- vector database
- semantic retrieval
- metadata filtering
- citation support

---

# 🧠 Structured AI Ticket Analysis

Old architecture used multiple AI calls for:

- title
- priority
- category
- escalation

Now everything uses ONE structured AI call.

AI returns:

```json
{
  "title": "...",
  "priority": "HIGH",
  "category": "BILLING",
  "shouldEscalate": true,
  "reason": "Repeated payment issue"
}
```

Mapped into:

```text
TicketAnalysisDTO
```

Benefits:

- deterministic flow
- faster execution
- fewer hallucinations
- reduced latency
- schema-driven outputs

---

# ⚡ AI Performance Optimizations

The system originally took ~3 minutes per response.

Optimizations implemented:

## Faster Models

| Purpose | Model |
|---|---|
| Intent Classification | llama3.2:3b |
| Final Response Generation | llama3.1 |

---

## Redis AI Response Caching

Added Redis caching for:

- conversations
- messages
- ticket searches
- AI responses

---

## Ollama Optimizations

Improved:

- timeout handling
- concurrency
- generation configs
- context usage

---

# 📚 RAG (Retrieval-Augmented Generation)

The system supports enterprise-grade RAG.

---

# 📄 Company Policy Ingestion Pipeline

Implemented production-ready ingestion pipeline.

Features:

- duplicate detection
- PDF hashing
- metadata enrichment
- version tracking
- semantic chunk storage

---

## CompanyPolicy Entity

Tracks:

```text
fileName
companyId
fileHash
version
uploadedAt
```

---

## Duplicate Detection

Before ingestion:

```text
generateHash(Resource pdf)
```

using:

```text
MD5 hashing
```

If same hash exists:

```text
ingestion skipped
```

---

## Policy Versioning

If:

```text
same filename + different content
```

then:

```text
version++
```

---

## Metadata Enrichment

Every chunk stores:

- source filename
- companyId
- uploadTime
- chunkId
- version

---

## Improved Retrieval

RAG retrieval includes:

- metadata filters
- source names
- citation page numbers
- company isolation

---

# 🎫 Ticket System

## Ticket Creation

Tickets can be created in 2 ways.

### Manual User Ticket

User directly creates ticket.

Conversation:

```text
null
```

---

### AI Escalation Ticket

AI creates ticket during conversation.

Conversation linked automatically.

---

# 🎯 Smart Category-Based Routing

Implemented intelligent ticket routing system.

---

## CategoryType Enum

```text
BILLING
TECHNICAL
GENERAL
DELIVERY
ACCOUNT
```

Every ticket has one category.

---

## Agent Expertise System

Agents can support multiple categories:

```java
Set<CategoryType> expertise
```

Stored using:

```java
@ElementCollection(fetch = EAGER)
```

---

## Smart Assignment Algorithm

System finds:

```text
least-loaded agent
WITH matching expertise
```

Fallback:

```text
least-loaded available agent
```

---

## Automatic Reassignment

If category changes:

```text
new appropriate agent assigned automatically
```

---

# 📝 Ticket Comments System

Supports ongoing communication between:

- user
- assigned agent

Features:

- comment entity
- ticket linkage
- author tracking
- secured access
- comment history

Endpoints:

```text
POST /tickets/{ticketId}/comments
GET /tickets/{ticketId}/comments
```

---

# 📊 Ticket Activity / Audit Log System

Implemented full audit logging system.

Tracks:

- ticket creation
- status changes
- priority changes
- category changes
- assignments
- comments

---

## TicketActivity Entity

Fields:

```text
ticket
actionType
performedBy
oldValue
newValue
createdAt
```

---

## Action Types

```text
CREATED
STATUS_CHANGED
PRIORITY_CHANGED
CATEGORY_CHANGED
ASSIGNED
COMMENT_ADDED
```

---

## History Endpoint

```text
GET /tickets/{ticketId}/history
```

Accessible only to:

- ticket creator
- assigned agent

---

# 📈 Ticket Status System

Supported states:

```text
OPEN
IN_PROGRESS
CLOSED
```

Agents can update status using:

```text
PUT /tickets/{ticketId}/status
```

If ticket closes:

```text
resolution email sent automatically
```

---

# 🔥 Manual Admin Features

## Manual Ticket Assignment

Admin can manually assign tickets.

Used when:

- AI assignment fails
- workload balancing needed
- manual intervention required

---

## Manual Priority Update

Allowed for:

- assigned agent
- admin

---

## Manual Category Update

Allowed for:

- assigned agent
- admin

Triggers:

```text
automatic reassignment
```

---

# 💬 Conversation System

## Get User Conversations

Endpoint:

```text
GET /conversations
```

Returns:

- user conversations
- latest ordered titles

Redis cached.

---

## Get Conversation Messages

Endpoint:

```text
GET /messages/{conversationId}
```

Returns:

- full conversation history
- AI/user messages

Redis cached.

---

## Rename Conversation

```text
PUT /conversations/{conversationId}/title
```

---

## Search Conversations

```text
GET /conversations/search?keyword=
```

Searches:

- titles
- messages

User-specific only.

---

## Close Conversations

```text
PUT /conversations/{conversationId}/close
```

Supports conversation archiving.

---

## Delete Conversations

```text
DELETE /conversations/{conversationId}
```

Supports:

- ownership validation
- safe deletion
- message cleanup

---

# 👤 User & Agent Endpoints

## Current User

```text
GET /users/me
```

Returns:

- id
- name
- email
- role

---

## Agent List

Admin-only endpoint:

```text
GET /agents
```

Returns:

- expertise categories
- active ticket counts

---

## Agent Tickets

```text
GET /agents/me/tickets
```

Returns assigned tickets.

---

# 📊 Dashboard Statistics

Endpoint:

```text
GET /dashboard/stats
```

Role-aware statistics:

- openTickets
- resolvedTickets
- highPriorityTickets
- totalTickets
- assignedTickets

---

# 🛡️ Security Features

## Role-Based Authorization

Supported roles:

```text
USER
AGENT
ADMIN
```

---

## Ownership Validation

Only:

- ticket creator
- assigned agent

can access protected ticket data.

---

## Prompt Injection Protection

Conversation history treated as:

```text
UNTRUSTED INPUT
```

System prompts protect orchestration logic.

---

## Rate Limiting

Implemented using:

```text
Redis
```

---

## Cache Eviction

Automatic cache eviction on:

- ticket updates
- category changes
- assignment changes
- comments

---

# 📦 Project Structure

```text
src/main/java
│
├── Controller
├── Service
├── Repository
├── Entity
├── DTO
├── Security
├── AI
├── Configuration
├── ToolCalling
├── Cache
├── Exception
└── Validation
```

---

# ⚙️ Setup Instructions

## Clone Repository

```bash
git clone https://github.com/your-username/AI-Customer-Support-Backend-System.git
cd AI-Customer-Support-Backend-System
```

---

## Pull Ollama Models

```bash
ollama pull llama3.1
ollama pull llama3.2:3b
ollama pull nomic-embed-text
```

---

## Start Services

```bash
docker compose up -d
```

Services:

- MariaDB
- Redis
- Ollama

---

## Run Application

```bash
./mvnw spring-boot:run
```

---

# 🎯 Final Architecture Summary

```text
AI provides reasoning.
Backend provides control.

LLM never directly performs actions.
All sensitive operations are deterministic.

This is production-grade AI architecture.
```

---

# 👨‍💻 Author

Developed by Priyanshu Karmakar.

---

# 📄 License

MIT License
