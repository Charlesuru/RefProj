#!/usr/bin/env swift
// Generates the PLACEHOLDER app icons for every environment (replace with the real brand icon later).
//   swift scripts/assets/generate-placeholder-icons.swift      (from the repository root, macOS only)
//
// Design: white "R" on an environment colour, with the environment name below it (no label in PROD),
// so DEV/SIT/STAGE builds are easy to tell apart on a device. PROD uses the brand primary from RefTheme.
// Outputs:
//   iOS      iosApp/Sources/Assets.xcassets/AppIcon[-Dev|-Sit|-Stage].appiconset/icon-1024.png (opaque, 1024 px)
//   Android  app/android/src/<main|dev|sit|stage>/res/drawable-nodpi/ic_launcher_foreground.png (432 px, transparent)
//            app/android/src/<main|dev|sit|stage>/res/values/ic_launcher_background.xml
import AppKit

struct Environment {
    let label: String? // nil = PROD
    let rgb: UInt32
    let iconSet: String
    let androidSourceSet: String
}

let environments = [
    Environment(label: nil, rgb: 0x1F5EFF, iconSet: "AppIcon", androidSourceSet: "main"),
    Environment(label: "DEV", rgb: 0x00876F, iconSet: "AppIcon-Dev", androidSourceSet: "dev"),
    Environment(label: "SIT", rgb: 0xC26A00, iconSet: "AppIcon-Sit", androidSourceSet: "sit"),
    Environment(label: "STAGE", rgb: 0x7B3FB0, iconSet: "AppIcon-Stage", androidSourceSet: "stage"),
]

func color(_ rgb: UInt32) -> NSColor {
    NSColor(
        srgbRed: CGFloat((rgb >> 16) & 0xFF) / 255,
        green: CGFloat((rgb >> 8) & 0xFF) / 255,
        blue: CGFloat(rgb & 0xFF) / 255,
        alpha: 1
    )
}

/// Draws the glyph and label centred in `content` (the visible area of the icon).
func drawMark(label: String?, in content: CGRect) {
    let style = NSMutableParagraphStyle()
    style.alignment = .center
    let glyphSize = content.height * (label == nil ? 0.62 : 0.52)
    let glyph = NSAttributedString(string: "R", attributes: [
        .font: NSFont.systemFont(ofSize: glyphSize, weight: .heavy),
        .foregroundColor: NSColor.white,
        .paragraphStyle: style,
    ])
    let caption = label.map {
        NSAttributedString(string: $0, attributes: [
            .font: NSFont.systemFont(ofSize: content.height * 0.15, weight: .bold),
            .foregroundColor: NSColor.white,
            .kern: content.height * 0.01,
            .paragraphStyle: style,
        ])
    }
    let glyphHeight = glyph.size().height
    let captionHeight = caption?.size().height ?? 0
    let total = glyphHeight + captionHeight
    let top = content.midY + total / 2 // AppKit origin is bottom-left
    glyph.draw(in: CGRect(x: content.minX, y: top - glyphHeight, width: content.width, height: glyphHeight))
    caption?.draw(in: CGRect(x: content.minX, y: top - total, width: content.width, height: captionHeight))
}

func render(size: Int, opaque: Bool, draw: (CGRect) -> Void) -> Data {
    let space = CGColorSpace(name: CGColorSpace.sRGB)!
    let info = opaque ? CGImageAlphaInfo.noneSkipLast.rawValue : CGImageAlphaInfo.premultipliedLast.rawValue
    let context = CGContext(
        data: nil, width: size, height: size, bitsPerComponent: 8, bytesPerRow: 0, space: space, bitmapInfo: info
    )!
    NSGraphicsContext.current = NSGraphicsContext(cgContext: context, flipped: false)
    draw(CGRect(x: 0, y: 0, width: size, height: size))
    NSGraphicsContext.current = nil
    let rep = NSBitmapImageRep(cgImage: context.makeImage()!)
    return rep.representation(using: .png, properties: [:])!
}

func write(_ data: Data, to path: String) throws {
    let url = URL(fileURLWithPath: path)
    try FileManager.default.createDirectory(at: url.deletingLastPathComponent(), withIntermediateDirectories: true)
    try data.write(to: url)
    print("→ \(path)")
}

let catalog = "iosApp/Sources/Assets.xcassets"
try write(Data("{\n  \"info\" : {\n    \"author\" : \"xcode\",\n    \"version\" : 1\n  }\n}\n".utf8), to: "\(catalog)/Contents.json")

for env in environments {
    // iOS: one 1024 px universal icon per set (Xcode generates the other sizes). Must be opaque.
    let ios = render(size: 1024, opaque: true) { rect in
        color(env.rgb).setFill()
        rect.fill()
        drawMark(label: env.label, in: rect.insetBy(dx: rect.width * 0.12, dy: rect.height * 0.12))
    }
    try write(ios, to: "\(catalog)/\(env.iconSet).appiconset/icon-1024.png")
    try write(Data("""
    {
      "images" : [
        {
          "filename" : "icon-1024.png",
          "idiom" : "universal",
          "platform" : "ios",
          "size" : "1024x1024"
        }
      ],
      "info" : {
        "author" : "xcode",
        "version" : 1
      }
    }

    """.utf8), to: "\(catalog)/\(env.iconSet).appiconset/Contents.json")

    // Android adaptive icon: 108 dp canvas at 4x; content stays inside the 66 dp safe zone.
    let android = render(size: 432, opaque: false) { rect in
        drawMark(label: env.label, in: rect.insetBy(dx: rect.width * 0.25, dy: rect.height * 0.25))
    }
    let res = "app/android/src/\(env.androidSourceSet)/res"
    try write(android, to: "\(res)/drawable-nodpi/ic_launcher_foreground.png")
    try write(Data(String(format: """
    <?xml version="1.0" encoding="utf-8"?>
    <!-- Generated by scripts/assets/generate-placeholder-icons.swift (placeholder icon). -->
    <resources>
        <color name="ic_launcher_background">#%06X</color>
    </resources>

    """, env.rgb).utf8), to: "\(res)/values/ic_launcher_background.xml")
}
