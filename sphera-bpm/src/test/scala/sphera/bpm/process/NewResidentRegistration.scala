package sphera.bpm.process

import akka.testkit.TestKit
import sphera.bpm.BpmBaseSpec
import sphera.bpm.masterdata.actor.NewMasterDataManager
import sphera.core.test.PersistenceSpec
import sphera.bpm.json._

trait NewResidentRegistration extends NewProcessManager
  with NewMasterDataManager
  with ProcessExpectations
  with BpmBaseSpec { _: PersistenceSpec with TestKit =>

  val enterDataOperationId = "EnterData"

  /**
   * Данные задачи заполнения анкеты
   */
  val dataA =
    s"""
       |{
       |  "value" : {
       |    "company" : {
       |      "value" : "Ltd Sphera"
       |    },
       |    "title" : {
       |      "description" : "updated",
       |      "value" : {
       |        "en" : {
       |          "value" : "Limited company \\"Sphera\\""
       |        },
       |        "ru" : {
       |          "value" : "Общество с ограниченной ответственностью \\"Sphera\\""
       |        }
       |      }
       |    },
       |    "employees" : {
       |      "value" : 10
       |    },
       |    "email" : {
       |      "value" : "it@kantemirov.ru"
       |    },
       |    "phone" : {
       |      "value" : "$generatePhone"
       |    }
       |  }
       |}
    """.stripMargin

  val enterDataUserTaskData = parse(dataA)

  /**
   * Данные задачи проверки анкеты
   */
  val dataB =
    s"""
       |{
       |  "value" : {
       |    "approved" : {
       |      "value" : false
       |    },
       |    "rejectReason" : {
       |      "value" : "Отказ в регистрации"
       |    }
       |  }
       |}
    """.stripMargin

  val validationUserTaskData = parse(dataB)

}