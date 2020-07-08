package quarantine.covid19.core

case class StateRiskList(elements: List[StateRisk])

case class StateRisk(id: String, name: String, value: Double)