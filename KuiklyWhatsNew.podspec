Pod::Spec.new do |spec|
  spec.name             = "KuiklyWhatsNew"
  spec.version          = "1.1.1"
  spec.summary          = "Cross-platform What's New component for KuiklyUI"
  spec.description      = <<-DESC
                        WhatsNew is a pure Kuikly DSL component (componentType: KuiklyView)
                        that shows a customizable "What's New" screen with automatic version
                        tracking, patch-version fallback and primary/secondary actions.
                        This pod ships the prebuilt XCFramework produced by GitHub Actions
                        on a macOS runner (Kotlin/Native iOS artifacts cannot be built on Linux).
                        DESC
  spec.homepage         = "https://github.com/Richard102257/kuikly-whatsnew-kit"
  spec.license          = { :type => "MIT", :file => "LICENSE" }
  spec.author           = { "Richard102257" => "lhui1022@gmail.com" }
  spec.ios.deployment_target = '13.0'

  # 预编译 XCFramework，由 .github/workflows/release-ios.yml 在 macOS runner 上构建并上传到 GitHub Release
  spec.source = {
    :http => "https://github.com/Richard102257/kuikly-whatsnew-kit/releases/download/v#{spec.version}/WhatsNew.xcframework.zip"
  }

  spec.vendored_frameworks = 'WhatsNew.xcframework'
  spec.static_framework    = true
  spec.libraries           = 'c++'

  spec.pod_target_xcconfig = {
    'VALID_ARCHS[sdk=iphonesimulator*]' => 'arm64 x86_64'
  }

  spec.dependency 'OpenKuiklyIOSRender', '~> 2.0'
end
