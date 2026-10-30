#!/usr/bin/env ruby
# 生成 iosApp.xcodeproj（纯脚本，避免仓库里塞一份手改过的 pbxproj）。
# 用法：ruby iosApp/generate_project.rb
require 'fileutils'
require 'xcodeproj'

root = File.expand_path(__dir__)
project_path = File.join(root, 'iosApp.xcodeproj')
FileUtils.rm_rf(project_path)

project = Xcodeproj::Project.new(project_path)
target = project.new_target(:application, 'iosApp', :ios, '15.0')

group = project.main_group.new_group('iosApp', 'iosApp')
%w[main.m AppDelegate.m KuiklyWhatsNewViewController.m].each do |name|
  target.add_file_references([group.new_file(name)])
end
%w[AppDelegate.h KuiklyWhatsNewViewController.h Info.plist].each do |name|
  group.new_file(name)
end

target.build_configurations.each do |config|
  settings = config.build_settings
  settings['INFOPLIST_FILE'] = 'iosApp/Info.plist'
  settings['PRODUCT_BUNDLE_IDENTIFIER'] = 'io.github.Richard102257.whatsnew.demo'
  settings['PRODUCT_NAME'] = 'iosApp'
  settings['TARGETED_DEVICE_FAMILY'] = '1'
  settings['IPHONEOS_DEPLOYMENT_TARGET'] = '15.0'
  settings['GENERATE_INFOPLIST_FILE'] = 'NO'
  settings['CODE_SIGNING_ALLOWED'] = 'NO'
  settings['CODE_SIGN_IDENTITY'] = ''
  settings['SWIFT_VERSION'] = '5.0'
  settings['CLANG_ENABLE_MODULES'] = 'YES'
  settings['ENABLE_USER_SCRIPT_SANDBOXING'] = 'NO'
  settings['ALWAYS_SEARCH_USER_PATHS'] = 'NO'
end

project.save

# ─── UI 测试 target：点一次「继续」，用来产生「已读」状态 ───
test_target = project.new_target(:ui_test_bundle, 'iosAppUITests', :ios, '15.0')
test_group = project.main_group.new_group('iosAppUITests', 'iosAppUITests')
test_target.add_file_references([test_group.new_file('WhatsNewUITests.swift')])
test_target.add_dependency(target)
test_target.build_configurations.each do |config|
  settings = config.build_settings
  settings['PRODUCT_BUNDLE_IDENTIFIER'] = 'io.github.Richard102257.whatsnew.demo.uitests'
  settings['TEST_TARGET_NAME'] = 'iosApp'
  settings['SWIFT_VERSION'] = '5.0'
  settings['GENERATE_INFOPLIST_FILE'] = 'YES'
  settings['CODE_SIGNING_ALLOWED'] = 'NO'
  settings['IPHONEOS_DEPLOYMENT_TARGET'] = '15.0'
end

project.save

# ─── 共享 scheme，让 xcodebuild test 能直接跑 ───
scheme = Xcodeproj::XCScheme.new
scheme.add_build_target(target)
scheme.set_launch_target(target)
scheme.add_test_target(test_target)
scheme.save_as(project_path, 'iosApp', true)

puts "generated #{project_path}"
