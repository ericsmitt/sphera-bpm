package sphera.bpm.utils

trait Printable {
  implicit class ConsoleString(underlying: String) {
    def red: String = Console.RED + underlying + Console.RESET
    def yellow: String = Console.YELLOW + underlying + Console.RESET
    def blue: String = Console.BLUE + underlying + Console.RESET
    def blueB: String = Console.BLUE_B + underlying + Console.RESET
  }
}
