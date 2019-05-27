# ウォーキング for Android

GoogleMap上に表示されたルートを歩いて★を集めるAndroidアプリです。  
東京都オープンデータアプリコンテスト2018に応募しました。

# はじめに
1. Android Studio 3 をダウンロードする。
2. 本リポジトリをクローンする。
3. Android Studio でソースを開く。

![Walking for Andorid](.github/app.jpg)

# リポジトリにコミットしていないリソース

## Kml

Kmlファイルは東京都が作成したものでリポジトリにはダミーを配置しているため以下を実施してください。

1. 東京都オープンデータカタログサイトからダウンロード

http://opendata-catalogue.metro.tokyo.jp/dataset/t000010d0000000056

 - 大量にあるのでcurlを活用する。  
 - zipを解凍する。

2. リネーム（Androidのリソースで利用可能にする）

kml_を頭につける。  
大文字を小文字にリネームする。

3. 置換1（バルーンに$[name]が表示されるので消す）

以下の箇所を削除する。
```
<BalloonStyle>
  <text><![CDATA[<h3>$[name]</h3>]]></text>
</BalloonStyle>
```
コマンド
```
grep -l '置換対象の文字列' ./* | xargs sed -i "" "s/置換対象の文字列/置換後の文字列/g"
または
find ./ -type f | xargs sed -i "" "s/xxx/yyy/g"
→これだと.DS_Storeが来るとエラーになるので注意。
→macのsedは-iの後に""を付けないといけないようだ。
```
```
grep -l '<text>' ./* | xargs sed -i "" 's/<text><!\[CDATA\[<h3>$\[name\]<\/h3>\]\]><\/text>//g'
find ./ -type f | xargs sed -i "" 's/<text><!\[CDATA\[<h3>$\[name\]<\/h3>\]\]><\/text>//g'
```

4. 置換2（ランドマーク表示を白い小さいアイコンにしてしまうため）

以下を削除する。
```
<href>http://www.gstatic.com/mapspro/images/stock/503-wht-blank_maps.png</href>
```
コマンド
```
find ./ -type f | xargs sed -i "" 's/<href>http:\/\/www.gstatic.com\/mapspro\/images\/stock\/503-wht-blank_maps.png<\/href>//g'
```

5. 配置

⁨ - app/⁨src/⁨main/⁨res 配下に配置する。⁩

## Constants.kt

初期値の区、タイトル、タイトル英字がダミーになっているので、正式なものに置換してください。

## API Key

リポジトリのAPI Keyはダミー値で、Google Mapが表示されないため以下の作業を実施する。

1. 発行

Google Cloud PlatformからMaps SDK for AndroidのAPI Keyを発行する。
https://cloud.google.com/maps-platform/
※クレジットカードが必要。2019年5月時点ではMap APIの利用は無料。  
デバッグ用とリリース用で2つ作成する。

2. 設定

1.で取得したAPI Keyを以下のファイルに設定する。
 - デバッグ用：app/src/debug/res/values/google_maps_api.xml
 - リリース用1：app/src/main/res/values/google_maps_api.xml
 - リリース用2：app/src/release/res/values/google_maps_api.xml

3. 証明書フィンガープリントの取得

 - リリースビルド（Google Play Storeに載せる）の場合
Google Play Consoleからリリース管理>アプリの署名からSHA-1の署
名をコピーする。

 - リリースビルド（ローカルでデバッグする）の場合
 Google Cloud Platformの認証情報ページに書いてあるkeytoolコマンドを実行する。

4. 証明書フィンガープリントの登録（リリースビルド）

Google Cloud Platformでリリース用のAPI Keyに2.で取得した証明書フィンガープリントを登録する。  
> API KeyがHackされても使えないようにします。

5. アプリの署名の登録

リリースビルドをローカルでデバッグする場合に以下を実施する。

app/build.gradle に以下を登録する。
```
config {
    keyAlias 'xxxx'
    keyPassword 'xxxx'
    storeFile file('xxxx.jks')
    storePassword 'xxxx'
}
```
※xxxxの値はアプリ署名の値を指定する。


# 開発用メモ

## DataBindingについて

* Observableは基本型しか使えない。

良い例
```
[activity.xml]
<View
        android:id="@+id/view"
        android:visibility="@{viewModel.topVisibility ? View.VISIBLE : View.INVISIBLE}"
/>

[ViewModel.kt]
var topVisibility = ObservableBoolean(true)
```

悪い例
```
[activity.xml]
<View
        android:id="@+id/topView"
        android:visibility="@{viewModel.topVisibility}"
/>

[ViewModel.kt]
var topVisibility = ObservableField(View.VISIBLE)
```
