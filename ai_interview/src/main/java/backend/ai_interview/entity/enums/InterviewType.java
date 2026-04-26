package backend.ai_interview.entity.enums;

/**
 * InterviewType
 *
 * Defines the category/type of interview session.
 *
 * TECHNICAL:
 * - Core technical questions (DSA, backend, frontend, system design, etc.)
 *
 * HR:
 * - Behavioral, personality, and HR questions
 *
 * MIXED:
 * - Combination of technical + HR questions
 *
 * PROJECT:
 * - Questions based on candidate's projects (resume / GitHub)
 *
 * RESUME:
 * - Questions derived directly from resume content
 *
 * BEHAVIORAL:
 * - Deep behavioral analysis (situational questions, STAR method, etc.)
 *
 * CODING:
 * - Coding-focused interview (DSA problems, logic building)
 */
@SuppressWarnings("all")
public enum InterviewType {

    TECHNICAL,

    HR,

    MIXED,

    PROJECT,

    RESUME,

    BEHAVIORAL,

    CODING
}