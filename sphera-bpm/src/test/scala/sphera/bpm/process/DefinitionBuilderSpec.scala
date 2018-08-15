//defstorage sphera.bpm.process.builder
//
//import sphera.bpm.defstorage.DefinitionBuilder
//import sphera.bpm.masterdata.actor.NewDataStructure
//import sphera.bpm.defstorage._
//import org.scalatest._
//import org.scalatest.compatible.Assertion
//
//class DefinitionBuilderSpec extends AsyncWordSpec
//  with Matchers
//  with DefinitionBuilder
//  with NewDataStructure
//  with ParallelTestExecution {
//  def assertDefinitionsA(p2: Definitions): Assertion = {
//    val y = p2.repr.withoutMetadata
//
//    y.repr should have size dataStructureA.elements.size
//    y.repr should contain key stringDataStructureElementIdA
//    y.repr should not contain key(stringDataStructureElementIdB)
//    y.repr should contain key booleanDataStructureElementIdA
//    y.repr should not contain key(booleanDataStructureElementIdB)
//    y.repr should contain key decimalDataStructureElementIdA
//    y.repr should not contain key(decimalDataStructureElementIdB)
//
//    // Test if values was substituted
//    y.repr(stringDataStructureElementIdA).value shouldBe p3(stringDataStructureElementIdA)
//    y.repr(booleanDataStructureElementIdA).value shouldBe p3(booleanDataStructureElementIdA)
//    y.repr(decimalDataStructureElementIdA).value shouldBe p3(decimalDataStructureElementIdA)
//  }
//  def assertDefinitionsB(p2: Definitions): Assertion = {
//    val y = p2.repr.withoutMetadata
//
//    y should have size dataStructureB.elements.size
//    y should contain key stringDataStructureElementIdB
//    y should not contain key(stringDataStructureElementIdA)
//    y should contain key booleanDataStructureElementIdB
//    y should not contain key(booleanDataStructureElementIdA)
//    y should contain key decimalDataStructureElementIdB
//    y should not contain key(decimalDataStructureElementIdA)
//
//    // Test if values was substituted
//    y(stringDataStructureElementIdB).value shouldBe stringDataStructureElementTupleB._2.defaultValue.get
//    y(booleanDataStructureElementIdB).value shouldBe booleanDataStructureElementTupleB._2.defaultValue.get
//    y(decimalDataStructureElementIdB).value shouldBe decimalDataStructureElementTupleB._2.defaultValue.get
//  }
//  def assertDefinitionsC(p2: Definitions): Assertion = {
//    val y = p2.repr.withoutMetadata
//
//    y.repr should have size dataStructureA.elements.size + dataStructureB.elements.size
//    y.repr should contain key stringDataStructureElementL1IdA
//    y.repr should contain key booleanDataStructureElementL1IdA
//    y.repr should contain key decimalDataStructureElementL1IdA
//
//    // Test if values was substituted
//    y.repr(stringDataStructureElementL1IdA).value shouldBe dataL1(stringDataStructureElementL1IdA)
//    y.repr(booleanDataStructureElementL1IdA).value shouldBe dataL1(booleanDataStructureElementL1IdA)
//    y.repr(decimalDataStructureElementL1IdA).value shouldBe dataL1(decimalDataStructureElementL1IdA)
//  }
//  "A DefinitionBuilder" when {
//    "generated p3 structure" must {
//      "build var defstorage with default values" in {
//        for {
//          p2 <- generateDataStructures()
//          y <- buildDefinitions(p2, p2.head._2.id, Map.empty).map(_.repr.values)
//        } yield {
//          y.map(_.description) should not contain None
//          y.map(_.value) shouldBe a[Any]
//        }
//      }
//    }
//    "dataStructureA" must {
//      "build var defstorage with default values" in {
//        for (p2 <- buildDefinitions(dataStructures, dataStructureIdA, Map.empty).map(_.repr.values)) yield {
//          p2.map(_.description) should not contain None
//          p2.map(_.value) shouldBe a[Any]
//        }
//      }
//      "build var defstorage with values" in {
//        for (p2 <- buildDefinitions(dataStructures, dataStructureIdA, p3))
//          yield assertDefinitionsA(p2)
//      }
//      //      "throw InvalidDataTypeException" in {
//      //        recoverToSucceededIf[InvalidDataTypeException] {
//      //          buildDefinitions(dataStructuresWithD, dataStructureIdD, Map(decimalDataStructureElementIdD -> "processId"))
//      //        }
//      //      }
//    }
//    "dataStructureB" must {
//      "build var defstorage with default values" in {
//        for (p2 <- buildDefinitions(dataStructures, dataStructureIdB, Map.empty).map(_.repr.values)) yield {
//          p2.map(_.description) should not contain None
//          p2.map(_.value) shouldBe a[Any]
//        }
//      }
//      "build var defstorage with values" in {
//        for (p2 <- buildDefinitions(dataStructures, dataStructureIdB, p3))
//          yield assertDefinitionsB(p2)
//      }
//    }
//    "dataStructureC" must {
//      "build var defstorage with default values" in {
//        for (p2 <- buildDefinitions(dataStructures, dataStructureIdC, Map.empty).map(_.repr.values)) yield {
//          p2.map(_.description) should not contain None
//          p2.map(_.value) shouldBe a[Any]
//        }
//      }
//      "build var defstorage with values" in {
//        for (p2 <- buildDefinitions(dataStructures, dataStructureIdC, dataL1))
//          yield assertDefinitionsC(p2)
//      }
//    }
//    "childNodes" must {
//      "return correct child nodes" in {
//        val p2 = childVars("a", dataD)
//        p2 should contain key "b.c"
//        p2 should not contain key("a.c")
//        p2 should contain key "c"
//        p2 should contain key "dateTime"
//        p2 should not contain key("c.a")
//      }
//      "return empty child nodes" in {
//        childVars("processId", dataD) shouldBe empty
//      }
//    }
//  }
//}