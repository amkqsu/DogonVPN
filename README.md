# DogonVPN

WireGuard tabanlı, DogonNote temasıyla (koyu + mor `#A78BFA`) uyumlu Android VPN istemcisi.
Kotlin + Jetpack Compose. WireGuard motoru için resmi `com.wireguard.android:tunnel` kütüphanesi kullanılıyor.

## Durum

Bu, çalışır bir **iskelet proje**: mimari, ekranlar, servis, widget, quick-settings tile,
QR okuma, split tunneling / kill switch / istatistik altyapısı hepsi yerinde. Gerçek cihazda
derleyip test ettikten sonra ince ayarlar (özellikle kill switch ve split tunneling UI'ları,
şu an "TODO" olan app-picker ve SSID-listesi ekranları) birlikte tamamlanacak.

## logo.png nereye konacak?

1. `logo.png` dosyanı (kare, şeffaf arka plan, en az 512×512) şuraya kopyala:
   `app/src/main/res/drawable-xxxhdpi/logo.png`
2. `app/src/main/res/drawable/ic_launcher_foreground.xml` dosyasını sil.
3. `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` ve `ic_launcher_round.xml` içinde
   `@drawable/ic_launcher_foreground` satırını `@drawable/logo` ile değiştir.

Kontrol panelinde (bildirim/widget) kullanılan saydam/monokrom PNG için:
- `app/src/main/res/drawable-xxxhdpi/logo_mono.png` olarak koy (yalnızca beyaz + şeffaf, status bar ikon kuralı böyle)
- `VpnNotifier.kt` içindeki `R.drawable.ic_notification` referansını `R.drawable.logo_mono` yap

## Termux ile derleme + GitHub'a push

```bash
# 1) Zip'i aç
cd ~/storage/downloads   # ya da zip'i nereye attıysan
unzip DogonVPN.zip -d ~/DogonVPN
cd ~/DogonVPN

# 2) Git deposu başlat (ilk kez ise)
git init
git add .
git commit -m "DogonVPN: ilk iskelet"

# 3) GitHub'da boş bir repo oluşturduktan sonra bağla
git remote add origin https://github.com/KULLANICI_ADIN/DogonVPN.git
git branch -M main
git push -u origin main
```

Sonraki her değişiklikte:
```bash
cd ~/DogonVPN
git add .
git commit -m "değişiklik açıklaması"
git push
```

## Android Studio ile derleme (önerilen)

Termux'ta doğrudan `gradlew assembleDebug` çalıştırmak SDK/NDK kurulumu gerektirir ve
yavaştır. En hızlı yol: repoyu GitHub'a push ettikten sonra bir bilgisayarda/Android
Studio'da `git clone` edip açmak — Gradle senkronu otomatik olur, `local.properties`
dosyası kendiliğinden oluşur (SDK yolu için `local.properties.example`'a bak).

## Mimari özeti

- `vpn/TunnelManager` — WireGuard `GoBackend` sarmalayıcısı, tek tünel
- `vpn/DogonVpnForegroundService` — tek/güncellenen bildirim, saniyelik hız/süre, istatistik örnekleme
- `stats/` — Room tabanlı günlük/aylık trafik geçmişi
- `tile/DogonTileService` — Hızlı Ayarlar panelinden aç/kapa
- `widget/DogonWidget` — Glance tabanlı ana ekran widget'ı
- `qr/` — CameraX + ML Kit ile canlı QR tarama ve galeriden QR okuma
- `data/ConfigStore` — WireGuard config'i `EncryptedSharedPreferences` ile şifreli saklar

## Bu sürümde tamamlananlar

- Split tunneling: Ayarlar > Split Tunneling ekranında yüklü uygulamalar listelenir,
  seçilenler `ExcludedApplications` olarak WireGuard config'ine otomatik enjekte edilir
- Wi-Fi İstisnaları: mevcut ağı tek dokunuşla ekleme + manuel SSID ekleme/kaldırma
- Gerçek otomatik bağlanma tetikleyicisi: `AutoConnectWatcher`, `ConnectivityManager`
  callback'i ile ağ değişikliklerini dinler; Wi-Fi ise SSID istisna listesine bakar

## Kalan / birlikte ince ayar yapılacaklar

- Gerçek "sızdırmaz" kill switch için Ayarlar ekranındaki "Sistem Düzeyinde Kill Switch"
  kısayolu kullanıcıyı Android'in kendi Always-on VPN ayarına yönlendiriyor; uygulama içi
  kill switch şu an sadece hızlı yeniden bağlanma deniyor (kodda `DogonVpnForegroundService`
  içinde yorumla açıklanmış)
- SSID okuma Android'de konum izni + konum servisleri açık gerektiriyor (işletim sistemi
  kısıtı); izin verilmeden Wi-Fi ağ adı okunamaz, bu yüzden izin akışı ekranda var
- Gerçek cihazda ilk derleme + WireGuard config testleri
