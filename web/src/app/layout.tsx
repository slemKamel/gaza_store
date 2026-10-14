import type { Metadata } from "next";
import localFont from "next/font/local";
import "./globals.css";

/**
 * IBM Plex Sans Arabic (OFL) — the same four weights the Android app bundles in
 * `app/src/main/res/font/`, self-hosted here so the dashboard is pixel- and
 * glyph-identical and works offline. License: ../../licenses/.
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

export const metadata: Metadata = {
  title: "لوحة تحكم غزة ستور",
  description: "إدارة محتوى وطلبات متجر غزة ستور",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html
      lang="ar"
      dir="rtl"
      className={`${plexArabic.variable} h-full antialiased`}
    >
      <body className="flex min-h-full flex-col font-sans">{children}</body>
    </html>
  );
}
