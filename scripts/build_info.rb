#!/usr/bin/env ruby
require "etc"
require "time"
require "open3"

class BuildInfo
  def initialize(pkg = nil, default_ver = "0.1.2")
    @pkg = pkg

    # Tenta pegar a versão do git
    git_ver, git_rev, dirty = fetch_git_info
    @ver = git_ver || default_ver
    @maj, @min, @pat = @ver.split('.').map(&:to_i)

    @rev = git_rev
    @rev += "-dirt" if dirty

    @date = Time.now.utc.strftime("%Y-%m-%d %H:%M:%S UTC")
    @user = Etc.getlogin || "unknown"
    @host = `hostname`.strip rescue "unknown"
    @os = "#{RUBY_PLATFORM} #{`uname -sr 2>/dev/null`.strip}"
    @ruby = "Ruby #{RUBY_VERSION}-p#{RUBY_PATCHLEVEL}"
    @java = `java -version 2>&1 | head -1`.strip rescue "Java (unknown)"
  end

  def fetch_git_info
    # commit hash curto
    rev = `git rev-parse --short HEAD 2>/dev/null`.strip
    return [nil, nil, false] if rev.empty?

    # version: tenta git describe --tags
    ver = `git describe --tags --abbrev=0 2>/dev/null`.strip
    ver = nil if ver.empty?

    # dirty check
    dirty = !`git status --porcelain 2>/dev/null`.strip.empty?

    [ver, rev, dirty]
  end

  def generate
    puts "package #{@pkg};" if @pkg
    puts ""
    puts "public final class BuildInfo {"
    puts "    private BuildInfo() {}"
    puts "    public static final String VERSION = \"#{@ver}\";"
    puts "    public static final int VERSION_MAJOR = #{@maj};"
    puts "    public static final int VERSION_MINOR = #{@min};"
    puts "    public static final int VERSION_PATCH = #{@pat};"
    puts "    public static final String GIT_REVISION = \"#{@rev}\";"
    puts "    public static final String BUILD_TIMESTAMP = \"#{@date}\";"
    puts "    public static final String BUILD_USER = \"#{@user}\";"
    puts "    public static final String BUILD_HOST = \"#{@host}\";"
    puts "    public static final String BUILD_OS = \"#{@os}\";"
    puts "    public static final String BUILD_RUBY = \"#{@ruby}\";"
    puts "    public static final String BUILD_JAVA = \"#{@java.gsub('"', "'")}\";"
    puts "    public static String getFullVersion() { return VERSION + \" (\" + GIT_REVISION + \")\"; }"
    puts "    public static String getBuildSummary() {"
    puts "        return String.format(\"Version: %s%nRevision: %s%nBuilt: %s by %s@%s%nOS: %s%nRuby: %s%nJava: %s\","
    puts "            VERSION, GIT_REVISION, BUILD_TIMESTAMP, BUILD_USER, BUILD_HOST, BUILD_OS, BUILD_RUBY, BUILD_JAVA);"
    puts "    }"
    puts "}"
  end
end

# Uso
pkg = ARGV[0] || "org.x96.sys.foundation"
BuildInfo.new(pkg).generate
