# AI Customer Support Backend System

---

## 🎯 Goal

Build a **production-grade AI-powered customer support backend system** that:

- Uses LLMs (LLaMA 3.1 via Ollama) for intelligent responses

- Supports **tool calling** (ticket creation, retrieval, etc.)

- Implements **RAG (Retrieval-Augmented Generation)** for company policies

- Handles **real-world support workflows**

- Mimics **enterprise support systems (like Zendesk / Freshdesk)**

---

## 🛠 Tech Stack

### Backend

- Java + Spring Boot

- Spring Security (JWT + OAuth2)

- Spring AI (LLM + Tool Calling)

- Hibernate / JPA

### AI / LLM

- Ollama (LLaMA 3.1)

- Tool Calling via Spring AI

- RAG Pipeline using Vector Store

### Database

- MySQL / PostgreSQL (assumed)

- Entities: User, Conversation, Message, Ticket

### Other

- Email Service (JavaMailSender)

- Rate Limiting Filter

- JWT Authentication

---

## 🧱 Architecture Overview

User → Chat API → ChatService
↓
Preprocessing (history + metadata)
↓
LLM (with tools)
↓
Tool Call OR Text Response
↓
Validation Layer
↓
Tool Execution (Ticket / DB / Email)
↓
Response stored in DB

## 🧩 Core Modules

### 1. Chat System

- Maintains conversation history

- Stores messages (USER / AI)

- Uses RAG + LLM for responses

### 2. Tool Calling System

AI has access to:

- `searchCompanyPolicy`

- `createSupportTicket`

- `getTicketDetails`

### 3. Ticket System

- Ticket creation via AI

- Assignment to least-loaded agent

- Status lifecycle:

  - OPEN → IN_PROGRESS → CLOSED

### 4. Authentication

- JWT-based login

- OAuth2 login (Google/GitHub)

### 5. Email System

- Sends HTML email when ticket is closed

---

## 🤖 AI Capabilities

### ✅ RAG (Retrieval Augmented Generation)

- Fetches company policies from vector DB

- Uses semantic similarity search

### ✅ Tool Calling

- AI can:

  - Create tickets

  - Fetch ticket details

  - Answer policy questions

### ✅ Context Awareness

- Uses conversation history

- Handles follow-up queries

### ✅ Frustration Detection

- Keyword-based scoring

- Passed to LLM as signal

---

## 🧠 Key Features Implemented

---

### 🔹 1. Intelligent Chat System

- Uses LLM + conversation history

- Context-aware responses

- Integrates with tools

---

### 🔹 2. AI-Based Ticket Creation

- AI decides when to escalate

- Backend ensures:

  - No duplicate tickets

  - Valid input

  - Priority handling

---

### 🔹 3. Duplicate Ticket Prevention

- Only **1 active ticket per conversation**

- Check:
existsByConversationIdAndStatusIn(OPEN, IN_PROGRESS)

### 🔹 4. Ticket Details Retrieval (AI-Powered)

Tool:
getTicketDetails(conversationId)

AI can:

- Return only requested info:

  - status

  - agent

  - full details

---

### 🔹 5. Follow-up Handling

- Detects messages like:

  - "still not resolved?"

  - "any update?"

- Uses:
isFollowUp = true

- AI fetches existing ticket instead of creating new one

---

### 🔹 6. Ticket Lifecycle Management

Controller:
PUT /tickets/{ticketId}/status

Rules:

- OPEN → IN_PROGRESS

- IN_PROGRESS → CLOSED

- CLOSED → ❌ no change

---

### 🔹 7. AI + Backend Hybrid Control

- AI decides → backend validates

- Backend enforces:

  - No duplicates

  - Valid transitions

  - Data integrity

---

### 🔹 8. Email Notification System

When ticket is CLOSED:

- Send HTML email to user

Includes:

- Ticket ID

- Description

- Priority

- Agent name

- Resolution time

---

### 🔹 9. OAuth2 Authentication

Endpoint:
/auth/oauth2/authorization/{provider}

Flow:

- Redirect → Provider (Google/GitHub)

- Callback → `/login/oauth2/code/{provider}`

- Handled by:
handleOAuth2loginrequest()

---

### 🔹 10. Rate Limiting

- Custom `RateLimitFilter`

- Protects API from abuse

---

## 🧪 Validation Layer (Important)

Inside tools:

### createSupportTicket:

- Validate priority

- Prevent duplicates

- Validate message length

- Auto-generate title

---

## ⚠️ Known Design Decisions

- ❌ No multiple tickets per conversation

- ❌ No AI-generated resolution summaries

- ✅ Backend is the final authority

- ✅ AI is advisory, not fully trusted

---

## 🚧 Known Limitations

- Fallback still uses string matching (not ideal)

- No structured tool-call detection yet

- Email sending is synchronous

- No UI (backend-only system)

---

## 🚀 Next Features (Future Roadmap)

### 1. Structured Tool Call Detection (Production Grade)

- Detect if tool was actually called

- Avoid string-based fallback

---

### 2. Async Email Queue

- Use Kafka / RabbitMQ

---

### 3. MCP Server Integration

- Allow external tools to connect dynamically

---

### 4. Ticket Analytics Dashboard

- Avg resolution time

- Agent workload

- Ticket trends

---

### 5. Multi-Ticket Support (Advanced)

- Allow multiple issues per conversation

---

### 6. AI Agent Assist (For Agents)

- Suggest replies to agents

- Auto-summarize tickets

---

## 📂 Key Entities

### User

- id, name, email, role, providerType

### Conversation

- id, user

### Message

- id, content, sender, conversation

### Ticket

- id, title, description, status, priority

- createdBy, assignedTo

- createdAt, updatedAt

---

## 🧭 Current Status

✅ Core system complete  

✅ AI + Tool Calling working  

✅ Ticket system functional  

✅ Email notifications implemented  

✅ OAuth2 working  

---

## 🎯 Next Step

👉 Move toward:

**Production-grade tool calling + system refinement + testing**

---

## 💡 Final Summary

This project is:

> A **real-world AI-powered backend system** that combines:

- LLM reasoning

- Tool execution

- Business logic validation

- Stateful conversations

- Enterprise support workflows

---