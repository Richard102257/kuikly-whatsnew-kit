Pod::Spec.new do |spec|
  spec.name             = "whatsnewIOS"
  spec.version          = "1.0.0"
  spec.summary          = "whatsnewIOS is the iOS native component of Kuikly WhatsNewKit"
  spec.description      = <<-DESC
                        whatsnewIOS provides the iOS platform-side native implementation
                        for Kuikly WhatsNewKit, a cross-platform changelog component.
                        DESC
  spec.homepage         = "https://github.com/Richard102257/kuikly-whatsnew-kit"
  spec.license          = { :type => "MIT", :file => "LICENSE" }
  spec.author           = { "Richard102257" => "" }
  spec.ios.deployment_target = '13.0'
  spec.source           = { :git => "https://github.com/Richard102257/kuikly-whatsnew-kit.git", :tag => "#{spec.version}" }

  spec.pod_target_xcconfig = {
        'VALID_ARCHS[sdk=iphonesimulator*]' => 'arm64 x86_64'
  }
  spec.dependency 'OpenKuiklyIOSRender', '~> 2.0'
  spec.requires_arc     = true
  spec.source_files = 'whatsnewIOS/Classes/**/*'
  spec.static_framework = true
  spec.libraries    = "c++"
end
