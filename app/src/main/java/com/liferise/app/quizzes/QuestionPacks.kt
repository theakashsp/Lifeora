package com.liferise.app.quizzes

import com.liferise.app.domain.model.AlarmCategory
import com.liferise.app.domain.model.AlarmDifficulty
import com.liferise.app.domain.model.QuizLanguage
import com.liferise.app.domain.model.QuizQuestion

object QuestionPacks {

    val starterQuestions: List<QuizQuestion> = listOf(
        // ====================================================================
        // 1. MATHEMATICS (Verified mental math, arithmetic, and logic)
        // ====================================================================
        QuizQuestion(
            id = "math_001",
            category = AlarmCategory.MATH,
            language = QuizLanguage.ENGLISH,
            questionText = "What is 27 × 4?",
            options = listOf("108", "98", "118", "104"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.EASY,
            explanation = "27 × 4 = (25 × 4) + (2 × 4) = 100 + 8 = 108.",
            sourceOrAttribution = "Mental Arithmetic Standards"
        ),
        QuizQuestion(
            id = "math_002",
            category = AlarmCategory.MATH,
            language = QuizLanguage.ENGLISH,
            questionText = "What is the square root of 289?",
            options = listOf("17", "19", "13", "23"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.MEDIUM,
            explanation = "17 × 17 = 289.",
            sourceOrAttribution = "Elementary Algebra"
        ),
        QuizQuestion(
            id = "math_003",
            category = AlarmCategory.MATH,
            language = QuizLanguage.ENGLISH,
            questionText = "Solve: 15 + (6 × 8) - 12",
            options = listOf("51", "48", "54", "63"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.MEDIUM,
            explanation = "Order of operations: 6 × 8 = 48. Then 15 + 48 - 12 = 51.",
            sourceOrAttribution = "BODMAS Standards"
        ),
        QuizQuestion(
            id = "math_004",
            category = AlarmCategory.MATH,
            language = QuizLanguage.ENGLISH,
            questionText = "If a shirt costs $80 and has a 25% discount, what is the final price?",
            options = listOf("$60", "$65", "$55", "$70"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.EASY,
            explanation = "25% of 80 is 20. 80 - 20 = $60.",
            sourceOrAttribution = "Practical Percentages"
        ),
        QuizQuestion(
            id = "math_005",
            category = AlarmCategory.MATH,
            language = QuizLanguage.ENGLISH,
            questionText = "What is the next number in the sequence: 3, 7, 15, 31, ...?",
            options = listOf("63", "61", "59", "65"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.HARD,
            explanation = "Each term is (previous × 2) + 1. 31 × 2 + 1 = 63.",
            sourceOrAttribution = "Sequence Logic"
        ),

        // ====================================================================
        // 2. KANNADA RIDDLES & PROVERBS (ಕನ್ನಡ ಒಗಟುಗಳು ಮತ್ತು ಗಾದೆಗಳು)
        // Authentic Unicode Kannada text with cultural attribution
        // ====================================================================
        QuizQuestion(
            id = "kan_001",
            category = AlarmCategory.KANNADA,
            language = QuizLanguage.KANNADA,
            questionText = "ಒಗಟು: \"ಅಂಗೈ ಅಗಲದ ಎಲೆಯ ಮೇಲೆ ಮುತ್ತಿನಂತಹ ಹನಿ, ಅದು ಮುಟ್ಟಿದರೆ ಮಾಯ. ಅದು ಏನು?\"",
            options = listOf("ತಾವರೆ ಎಲೆಯ ಮೇಲಿನ ನೀರಹನಿ", "ಕಣ್ಣೀರು", "ಹಿಮದ ಹನಿ", "ಮಳೆಯ ಹನಿ"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.MEDIUM,
            explanation = "ತಾವರೆ ಎಲೆಯ ಮೇಲೆ ಬೀಳುವ ನೀರಿನ ಹನಿ ಮುತ್ತಿನಂತೆ ಹೊಳೆಯುತ್ತದೆ ಮತ್ತು ಮುಟ್ಟಿದರೆ ಜಾರಿಹೋಗುತ್ತದೆ.",
            sourceOrAttribution = "ಕನ್ನಡ ಜಾನಪದ ಒಗಟುಗಳು (Folk Riddles of Karnataka)"
        ),
        QuizQuestion(
            id = "kan_002",
            category = AlarmCategory.KANNADA,
            language = QuizLanguage.KANNADA,
            questionText = "ಗಾದೆ ಮಾತು ಪೂರ್ಣಗೊಳಿಸಿ: \"ಹಾಸಿಗೆ ಇದ್ದಷ್ಟು ...\"",
            options = listOf("ಕಾಲು ಚಾಚು", "ಮಲಗಬೇಕು", "ನಿದ್ರೆ ಮಾಡು", "ಮನೆ ಕಟ್ಟು"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.EASY,
            explanation = "ಗಾದೆ: 'ಹಾಸಿಗೆ ಇದ್ದಷ್ಟು ಕಾಲು ಚಾಚು' - ನಮ್ಮ ಮಿತಿಗಳನ್ನು ತಿಳಿದು ಬದುಕಬೇಕು ಎಂಬ ಅರ್ಥ ನೀಡುತ್ತದೆ.",
            sourceOrAttribution = "ಪ್ರಸಿದ್ಧ ಕನ್ನಡ ಗಾದೆಗಳು (Kannada Proverbs)"
        ),
        QuizQuestion(
            id = "kan_003",
            category = AlarmCategory.KANNADA,
            language = QuizLanguage.KANNADA,
            questionText = "ಒಗಟು: \"ಹಸಿರು ಗಿಡದ ಮೇಲೆ ಬಿಳಿ ಹೂವು, ಕಿತ್ತರೆ ವಾಸನೆ ಇಲ್ಲ, ತಿಂದರೆ ರುಚಿ ಇಲ್ಲ. ಅದು ಏನು?\"",
            options = listOf("ಸುಣ್ಣ (Chuna)", "ಹತ್ತಿ", "ಅಣಬೆ", "ಉಪ್ಪು"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.MEDIUM,
            explanation = "ಸಾಂಪ್ರದಾಯಿಕ ಕನ್ನಡ ಒಗಟಿನಲ್ಲಿ ವೀಳ್ಯದೆಲೆಗೆ ಹಚ್ಚುವ ಸುಣ್ಣವನ್ನು ಹೀಗೆ ಬಣ್ಣಿಸಲಾಗುತ್ತದೆ.",
            sourceOrAttribution = "ಕರ್ನಾಟಕ ಜಾನಪದ ಸಾಹಿತ್ಯ"
        ),
        QuizQuestion(
            id = "kan_004",
            category = AlarmCategory.KANNADA,
            language = QuizLanguage.KANNADA,
            questionText = "ಗಾದೆ ಮಾತು ಪೂರ್ಣಗೊಳಿಸಿ: \"ಉಪ್ಪಿಗಿಂತ ರುಚಿಯಿಲ್ಲ, ... ಗಿಂತ ಬಂಧುವಿಲ್ಲ.\"",
            options = listOf("ತಾಯಿ", "ತಂದೆ", "ಗುರು", "ಸ್ನೇಹಿತ"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.EASY,
            explanation = "'ಉಪ್ಪಿಗಿಂತ ರುಚಿಯಿಲ್ಲ, ತಾಯಿಗಿಂತ ಬಂಧುವಿಲ್ಲ' ಎಂಬುದು ಮಾತೃಪ್ರೇಮದ ಮಹಿಮೆಯನ್ನು ಸಾರುವ ಗಾದೆ.",
            sourceOrAttribution = "ಕನ್ನಡ ಗಾದೆಗಳ ಭಂಡಾರ"
        ),
        QuizQuestion(
            id = "kan_005",
            category = AlarmCategory.KANNADA,
            language = QuizLanguage.KANNADA,
            questionText = "ಒಗಟು: \"ಹುಟ್ಟುತ್ತಲೇ ಮುಂಡಾಸು ಹೊತ್ತು ಬರುವವನು ಯಾರು?\"",
            options = listOf("ಅಡಿಕೆ ಕಾಯಿ (Betel Nut)", "ತೆಂಗಿನಕಾಯಿ", "ಬದನೆಕಾಯಿ", "ಹಲಸಿನ ಹಣ್ಣು"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.HARD,
            explanation = "ಅಡಿಕೆ ಅಥವಾ ಬದನೆಕಾಯಿಯ ಮೇಲಿರುವ ತೊಟ್ಟನ್ನು ಮುಂಡಾಸಿಗೆ (ಮುಂಡಾಸು/ಪೇಟ) ಹೋಲಿಸಲಾಗುತ್ತದೆ.",
            sourceOrAttribution = "ಜಾನಪದ ಕವಿ ಪರಂಪರೆ"
        ),

        // ====================================================================
        // 3. GENERAL KNOWLEDGE (Science, History, Geography)
        // ====================================================================
        QuizQuestion(
            id = "gk_001",
            category = AlarmCategory.GENERAL_KNOWLEDGE,
            language = QuizLanguage.ENGLISH,
            questionText = "What is the powerhouse organelle of the eukaryotic cell?",
            options = listOf("Mitochondria", "Ribosome", "Nucleus", "Endoplasmic Reticulum"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.EASY,
            explanation = "Mitochondria generate most of the cell's ATP supply used as chemical energy.",
            sourceOrAttribution = "Cellular Biology Fundamentals"
        ),
        QuizQuestion(
            id = "gk_002",
            category = AlarmCategory.GENERAL_KNOWLEDGE,
            language = QuizLanguage.ENGLISH,
            questionText = "Which UNESCO World Heritage site was the capital of the Vijayanagara Empire?",
            options = listOf("Hampi", "Badami", "Pattadakal", "Belur"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.MEDIUM,
            explanation = "Hampi in Karnataka was the historic capital of the Vijayanagara Empire in the 14th century.",
            sourceOrAttribution = "Indian Archaeological Survey"
        ),
        QuizQuestion(
            id = "gk_003",
            category = AlarmCategory.GENERAL_KNOWLEDGE,
            language = QuizLanguage.ENGLISH,
            questionText = "Which planet in our solar system has the highest density?",
            options = listOf("Earth", "Jupiter", "Mercury", "Venus"),
            correctOptionIndex = 0,
            difficulty = AlarmDifficulty.MEDIUM,
            explanation = "Earth has the highest average density of about 5.514 g/cm³.",
            sourceOrAttribution = "NASA Planetary Factsheet"
        )
    )
}
