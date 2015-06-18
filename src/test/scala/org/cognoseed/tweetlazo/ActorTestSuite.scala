package org.cognoseed.tweetlazo

import akka.actor.{Props, ActorSystem}
import akka.testkit.{TestActorRef, TestKit}
import org.scalatest.{FunSuiteLike, BeforeAndAfterAll}

class ActorTestSuite extends TestKit(ActorSystem("TestSystem")) with FunSuiteLike with BeforeAndAfterAll {

  override def afterAll(): Unit = {
    system.shutdown()
  }

  test("system should have 3 actors if TweetDispatcher is started with a 2-item array argument") {
    val dispatcher = TestActorRef(new TweetDispatcher(Array("#test1", "#test2")))
    awaitCond(dispatcher.underlyingActor.children.size() == 2)
  }

}