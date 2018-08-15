package sphera.bpm.storage.json

import io.circe.Json
import io.circe.parser.parse

trait NewJsonStorage {
  val json: Json = parse("""
  {
    "order": {
      "customer": {
        "name": "Custy McCustomer",
        "contactDetails": {
          "address": "1 Fake Street, London, England",
          "phone": "0123-456-789"
        }
      },
      "items": [{
        "id": 2123,
        "description": "banana",
        "quantity": 1
      },
      {
        "id": 456,
        "description": "apple",
        "quantity": 2
      }],
      "total": 123.45
    }
  }
  """).getOrElse(Json.Null)

  val jsonStorage = JsonStorage(json)

  val jsonA: Json = parse("""
  {
    "order": {
      "customer": {
        "name": "updated"
      },
      "total": 150
    }
  }
  """).getOrElse(Json.Null)
}
