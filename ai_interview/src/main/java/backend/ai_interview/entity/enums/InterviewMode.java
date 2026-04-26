package backend.ai_interview.entity.enums;

/**
 * InterviewMode
 *
 * Defines how the AI interview should behave.
 *
 * MOCK:
 * - practice-oriented
 * - can provide hints
 * - can provide sample answers
 * - gives stronger learning guidance
 *
 * REAL:
 * - stricter interview simulation
 * - limited or no full-answer help
 * - evaluation-focused
 */
@SuppressWarnings("all")
public enum InterviewMode {
    MOCK,
    REAL
}