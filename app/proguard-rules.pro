# 既定の最適化ルールを利用。必要に応じて keep ルールを追記する。
# Readability4J / Jsoup はリフレクションを使わないため特別な keep は不要。
#
# Room / Hilt / OkHttp / Okio は各ライブラリ同梱の consumer ルールで保護されるため追記不要。

# kuromoji: 形態素解析辞書を jar 内リソースから読み込む。クラス・リソースを保持する。
-keep class com.atilika.kuromoji.** { *; }
-dontwarn com.atilika.kuromoji.**

# Readability4J（内部で kotlin リフレクション等を持たないが警告抑制）
-dontwarn net.dankito.readability4j.**

# Tink（security-crypto / EncryptedSharedPreferences が依存）はコンパイル専用の
# errorprone アノテーションを参照する。実行時には存在しないため警告を抑制する。
-dontwarn com.google.errorprone.annotations.**

# slf4j: jsoup/readability4j がオプション依存として参照するが本アプリは束ねていない。
-dontwarn org.slf4j.impl.StaticLoggerBinder
