package pages.common

import org.openqa.selenium.WebDriver
import org.scalatest.selenium.WebBrowser.find
import org.scalatest.selenium.WebBrowser.id
import org.scalatest.selenium.WebBrowser.Element
import views.common.Help.HelpLinkId

object HelpPanel {
  def help(implicit driver: WebDriver): Element = find(id(HelpLinkId)).get
}