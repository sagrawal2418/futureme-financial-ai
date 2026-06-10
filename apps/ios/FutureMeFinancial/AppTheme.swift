import SwiftUI
import UIKit

enum AppTheme {
    static let ink = adaptive(light: 0x11251F, dark: 0xEDF7F1)
    static let forest = Color(red: 21 / 255, green: 59 / 255, blue: 48 / 255)
    static let mint = Color(red: 166 / 255, green: 242 / 255, blue: 207 / 255)
    static let softMint = adaptive(light: 0xE5F6ED, dark: 0x1D3A2F)
    static let canvas = adaptive(light: 0xF3F6F2, dark: 0x0D1612)
    static let surface = adaptive(light: 0xFFFFFF, dark: 0x14211C)
    static let surfaceVariant = adaptive(light: 0xF0FAF5, dark: 0x1A2A23)
    static let line = adaptive(light: 0xDDE5DF, dark: 0x2A3B34)
    static let muted = adaptive(light: 0x6F7D76, dark: 0xA7B6AF)
    static let positive = adaptive(light: 0x23805E, dark: 0x61C99E)
    static let warning = adaptive(light: 0xB05E48, dark: 0xFFB4A4)

    private static func adaptive(light: UInt32, dark: UInt32) -> Color {
        Color(
            uiColor: UIColor { traits in
                UIColor(hex: traits.userInterfaceStyle == .dark ? dark : light)
            }
        )
    }
}

extension View {
    func futureMeCard(fill: Color = AppTheme.surface, showsBorder: Bool = true) -> some View {
        self
            .background(fill)
            .clipShape(RoundedRectangle(cornerRadius: 20, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: 20, style: .continuous)
                    .stroke(AppTheme.line, lineWidth: showsBorder ? 1 : 0)
            )
    }
}

private extension UIColor {
    convenience init(hex: UInt32) {
        self.init(
            red: CGFloat((hex >> 16) & 0xFF) / 255,
            green: CGFloat((hex >> 8) & 0xFF) / 255,
            blue: CGFloat(hex & 0xFF) / 255,
            alpha: 1
        )
    }
}
