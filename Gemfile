# Ruby tooling for local and CI builds. Install with:  bundle install
# Always run fastlane through Bundler so everyone uses the locked versions:
#   bundle exec fastlane <platform> <lane>
source "https://rubygems.org"

ruby file: ".ruby-version"

gem "fastlane", "~> 2.241"

plugins_path = File.join(File.dirname(__FILE__), "fastlane", "Pluginfile")
eval_gemfile(plugins_path) if File.exist?(plugins_path)
