package quarantine.covid19.core


//import 
/**
 * An alert is an object that captures specific state of CovidSnapshots
 *  and Annotations that are worth paying attention to.
 *  
 *  @param alertLevel   The type of alert - county, country, state, mixed level
 *  @param metric       The metric associated with this alert
 *  @param windowSize   The averaging window associated with this alert
 *  @param deltaDays    The time in days over which the alert is being computed 
 *  @param description  A succinct description of the alert
 *  @param locales      The list of locations related to the alert
 *  
 *  @author rajdeep
 */
case class Alert(alertLevel: String, metric: String, windowSize: String, deltaDays: Int, uiInfo: AlertUIInfo)

/**
 *  The info that drives the UI presets
 *  @param description: A succinct description of the alert
 *  @param locales: The list of locations related to the alert
 *  
 *  @author rajdeep
 */
case class AlertUIInfo(description: String, locales: List[String])
