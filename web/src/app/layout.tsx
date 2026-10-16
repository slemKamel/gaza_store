import type { Metadata } from "next";
import localFont from "next/font/local";
import "./globals.css";

/**
 * Five families, self-hosted from ./fonts and bundled at build time — the same
 * files the Android app ships in `app/src/main/res/font/`, so both surfaces are
 * glyph-identical and work offline. Licenses: ../../licenses/.
 *
 * They are wired into the `--font-*` roles in `globals.css`:
 *   Bodoni Moda      -> --font-logo      (Latin wordmark)
 *   Playfair Display -> --font-heading   (Latin headings, italic accents)
 *   Inter            -> --font-body      (Latin body, figures)
 *   IBM Plex Arabic  -> --font-body-ar   (Arabic UI/body)
 *   Amiri            -> --font-logo-ar   (the Arabic wordmark "متجر غزة")
 */
const plexArabic = localFont({
  src: [
    { path: "./fonts/ibm_plex_sans_arabic_regular.ttf", weight: "400", style: "normal" },
    { path: "./fonts/ibm_plex_sans_arabic_medium.ttf", weight: "500", style: "normal" },
    { path: "./fonts/ibm_plex_sans_arabic_semibold.ttf", weight: "600", style: "normal" },
    { path: "./fonts/ibm_plex_sans_arabic_bold.ttf", weight: "700", style: "normal" },
  ],
  variable: "--font-plex-arabic",
  display: "swap",
});

const amiri = localFont({
  src: [
    { path: "./fonts/amiri_regular.ttf", weight: "400", style: "normal" },
    { path: "./fonts/amiri_bold.ttf", weight: "700", style: "normal" },
  ],
  variable: "--font-amiri",
  display: "swap",
});

const inter = localFont({
  src: [
    { path: "./fonts/inter_regular.ttf", weight: "400", style: "normal" },
    { path: "./fonts/inter_medium.ttf", weight: "500", style: "normal" },
    { path: "./fonts/inter_semibold.ttf", weight: "600", style: "normal" },
  ],
  variable: "--font-inter",
  display: "swap",
});

const bodoniModa = localFont({
  src: [
    { path: "./fonts/bodoni_moda_regular.ttf", weight: "400", style: "normal" },
    { path: "./fonts/bodoni_moda_semibold.ttf", weight: "600", style: "normal" },
  ],
  variable: "--font-bodoni",
  display: "swap",
});

const playfairDisplay = localFont({
  src: [
    { path: "./fonts/playfair_display_regular.ttf", weight: "400", style: "normal" },
    { path: "./fonts/playfair_display_semibold.ttf", weight: "600", style: "normal" },
    { path: "./fonts/playfair_display_bold.ttf", weight: "700", style: "normal" },
    { path: "./fonts/playfair_display_italic.ttf", weight: "400", style: "italic" },
  ],
  variable: "--font-playfair",
  display: "swap",
});

export const metadata: Metadata = {
  title: "متجر غزة — لوحة التحكم",
  description: "إدارة محتوى وطلبات متجر غزة",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="ar"
      dir="rtl"
      className={`${plexArabic.variable} ${amiri.variable} ${inter.variable} ${bodoniModa.variable} ${playfairDisplay.variable} h-full antialiased`}
    >
      <body className="flex min-h-full flex-col font-sans">{children}</body>
    </html>
  );
}
