# ✍️ Free Grammar Checker

A free grammar checker with auto-correct and file upload, built with Spring Boot and LanguageTool. Packaged with Docker and built automatically with GitHub Actions CI/CD.

## Features

- Check text and see every mistake with a suggested fix
- Auto-correct the whole text in one click
- Upload a `.txt` or `.docx` file and download the corrected version
- Runs fully on your own machine, with no usage limits

## Tech Stack

- **Backend:** Java 17, Spring Boot, Gradle
- **Grammar engine:** LanguageTool (self-hosted)
- **Container:** Docker, Docker Compose
- **CI/CD:** GitHub Actions, GitHub Container Registry

## How it works

```
Browser → Spring Boot app → LanguageTool server
```

## CI/CD Pipeline

On every push to `main`, GitHub Actions will:

1. Build the app with Gradle
2. Run the tests
3. Build the Docker image
4. Push the image to GitHub Container Registry (ghcr.io)

## Run it locally

You need [Docker](https://docs.docker.com/get-docker/) installed.

```bash
git clone https://github.com/ravi-cloud511/free-grammar-checker.git
cd free-grammar-checker
docker compose up
```

Then open http://localhost:8080 in your browser.

The first start can take a few minutes because LanguageTool needs to download and start. Give it about 2 GB of free RAM.

## API

| Endpoint | Method | Description |
|---|---|---|
| `/health` | GET | Health check |
| `/api/check` | POST | Check text (JSON: `text`, `lang`) |
| `/api/upload` | POST | Upload `.txt` or `.docx`, get corrected file |

## Known limits

- Corrected `.docx` files come back as plain text (bold, tables and other formatting are not kept)
- The Docker image is built for x86-64 machines

## Author

**Ravi Sharma** — [GitHub](https://github.com/ravi-cloud511)
