# Ruby tooling for local and CI builds. Install with:  bundle install
# Always run fastlane through Bundler so everyone uses the locked versions:
#   bundle exec fastlane <platform> <lane>
source "https://rubygems.org"

ruby file: ".ruby-version"

gem "fastlane", "~> 2.241"

# fastlane plugins. A literal path: Dependabot only parses uninterpolated eval_gemfile arguments.
eval_gemfile("fastlane/Pluginfile")
