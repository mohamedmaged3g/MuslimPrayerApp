package com.muslim.prayerapp.data.repository

data class SunanCategory(
    val titleArabic: String,
    val items: List<SunanItem>
)

data class SunanItem(
    val titleArabic: String,
    val descriptionArabic: String,
    val hadithProofArabic: String? = null
)

object DailySunanRepository {

    val categories = listOf(
        SunanCategory(
            titleArabic = "سنن الجمعة",
            items = listOf(
                SunanItem("الاغتسال والتطيب", "الاهتمام بالنظافة والتطيب والتسوك يوم الجمعة"),
                SunanItem("قراءة سورة الكهف", "قراءة سورة الكهف لنور يضيء بين الجمعتين"),
                SunanItem("الإكثار من الصلاة على النبي", "«أَكْثِرُوا عَلَيَّ مِنَ الصَّلَاةِ فِيهَا»"),
                SunanItem("التبكير للمسجد", "الذهاب مبكراً لصلاة الجمعة والدعاء في ساعة الاستجابة")
            )
        ),
        SunanCategory(
            titleArabic = "السنن الراتبة للصلاة",
            items = listOf(
                SunanItem("سنة الفجر", "ركعتان قبل الفجر (خير من الدنيا وما فيها)"),
                SunanItem("سنة الظهر", "أربع ركعات قبل الظهر وركعتان بعدها"),
                SunanItem("سنة المغرب", "ركعتان بعد صلاة المغرب"),
                SunanItem("سنة العشاء", "ركعتان بعد صلاة العشاء"),
                SunanItem("صلاة الوتر", "ركعة أو ثلاث ركعات ختاماً لصلاة الليل")
            )
        ),
        SunanCategory(
            titleArabic = "سنن النوم والاستيقاظ",
            items = listOf(
                SunanItem("النوم على طهارة", "الوضوء قبل النوم والنوم على الشق الأيمن"),
                SunanItem("نفث المعوذات", "قراءة المعوذات وآية الكرسي والمسح على الجسد"),
                SunanItem("مسح أثر النوم والذكر", "قول «الحَمْدُ لِلَّهِ الَّذِي أَحْيَانَا بَعْدَ مَا أَمَاتَنَا»")
            )
        ),
        SunanCategory(
            titleArabic = "سنن الطعام والشراب",
            items = listOf(
                SunanItem("التسمية بالأكل", "قول «بِسْمِ اللَّهِ» والأكل باليمين ومما يليك"),
                SunanItem("حمد الله بعد الطعام", "قول «الْحَمْدُ لِلَّهِ الَّذِي أَطْعَمَنِي هَذَا وَرَزَقَنِيهِ»"),
                SunanItem("عدم ذم الطعام", "كان النبي ﷺ لا يذم طعاماً قط، إن اشتهاه أكله وإلا تركه")
            )
        )
    )
}
