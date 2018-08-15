package sphera.bpm.masterdata.actor

import java.time.LocalDate

import akka.actor.ActorRef
import akka.testkit.TestKit
import sphera.bpm.masterdata.model._
import sphera.core.test.PersistenceSpec

import scala.concurrent.Future
import scala.util.Random

trait NewCalendarTypesRepository { _: PersistenceSpec with TestKit =>
  def newCalendarTypesRepositoryActor(state: CalendarTypesState = CalendarTypesState()): Future[ActorRef] = Future {
    val id = generateActorId
    system.actorOf(CalendarTypesRepositoryActor.props(id, state), id.name)
  }

  def generateDeviations(min: Int = 2017, max: Int = 2020): Set[UnOrdinaryDay] = {
    val random = new Random()
    val year: Int = random.nextInt(max + 1 - min) + min
    (for (i <- min to year) yield {
      val id = generateId
      UnOrdinaryDay(
        generateDay(),
        s"p2-$i",
        Option(s"descr-$i"))
    }).toSet
  }

  def generateDay(min: Int = 0, max: Int = 9): LocalDate = {
    val random = new Random()
    val n: Int = random.nextInt(max + 1 - min) + min
    val days = List(LocalDate.of(2017, 1, 1), LocalDate.of(2017, 2, 12), LocalDate.of(2017, 3, 4),
      LocalDate.of(2017, 5, 12), LocalDate.of(2017, 8, 4), LocalDate.of(2018, 10, 10), LocalDate.of(2017, 12, 12),
      LocalDate.of(2017, 3, 14), LocalDate.of(2017, 5, 4), LocalDate.of(2018, 11, 10))

    days.apply(n)

  }

  def generateUpdateCalendarType(id: CalendarType.Id = generateUUID): Future[UpdateCalendarType] = Future {
    val i = generateId
    UpdateCalendarType(
      id,
      s"p2-$i",
      Option(s"descr-$i"),
      generateDeviations(),
      generateUUID)
  }
}
