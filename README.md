# 📦 ميديا بوكس | MediaBox

**MediaBox** هو تطبيق Android مفتوح المصدر ومبني بأحدث تقنيات **Kotlin + Jetpack Compose** ومعمارية **Clean Architecture + MVI** مخصص لتنزيل الوسائط المتعددة (فيديو وصوت) من مختلف المنصات الشائعة عبر حقل إدخال موحد.

---

## 🏗️ معمارية وقواعد التطبيق (Project Architecture)

المشروع مصمم بهيكلية **Modular Architecture**:

- **`app/`**: واجهة المستخدم الرئيسية المصممة بـ Jetpack Compose وتدعم اللغة العربية اتجاه اليمين إلى اليسار (`RTL`).
- **`core/extractor/`**: المحرك المركزي لمحللات المنصات (`CompositeExtractor.kt`, `YtDlpExtractor.kt`, `FacebookExtractor.kt`, `YouTubeExtractor.kt`, `TikTokExtractor.kt`, `TelegramExtractor.kt`).
- **`core/downloader/`**: مدير التنزيل الفعلي الشبكي (`FileDownloader.kt`) الذي يحفظ الوسائط بايت ببايت ببطاقة نسبة مئوية حقيقية.
- **`core/model/`**: نماذج البيانات الرئيسية (`PlatformType.kt`, `MediaInfo.kt`, `StreamOption.kt`, `DownloadTask.kt`).

---

## 📁 مسار الحفظ التلقائي
تطبيق **MediaBox** يحفظ جميع الوسائط المحملة داخل مجلد مستقل في الذاكرة الخارجية:
`Storage/Downloads/MediaBox/`

---

## 🚀 كيفية تجميع وبناء التطبيق من Termux
```bash
pkg install openjdk-17 git python ffmpeg -y
git clone [https://github.com/tombad8900/MediaBox.git](https://github.com/tombad8900/MediaBox.git)
cd MediaBox
chmod +x gradlew termux-setup.sh
./gradlew assembleDebug
