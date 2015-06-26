package org.cognoseed.tweetlazo

import akka.actor.SupervisorStrategy.Stop
import akka.actor._
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import org.cognoseed.tweetlazo.TweetDispatcher.{UnwatchHashtag, WatchHashtag}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FunSuiteLike}
import twitter4j.{HashtagEntity, Status}

class TweetDispatcherTestSuite extends TestKit(ActorSystem("TestSystem")) with FunSuiteLike with BeforeAndAfterAll
  with MockFactory {

  override def afterAll(): Unit = {
    shutdown(system)
  }

  // TODO split tests into ones that need the TestActorRef, and ones that don't.
  trait CommonTweetDispatcherTest {
    val counterProbe = TestProbe()
    val maker = mockFunction[ActorRefFactory, String, ActorRef]
    maker.expects(*, "test").returning(counterProbe.ref)
    val dispatcher = TestActorRef[TweetDispatcher](TweetDispatcher.props(maker))
    dispatcher ! WatchHashtag("test")
  }

  trait ExceptionThrowingTest {
    val maker = mockFunction[ActorRefFactory, String, ActorRef]
    maker.expects(*, "test").returning(testActor)
    val dispatcher = system.actorOf(Props(new Actor {
      val realDispatcher = context.actorOf(TweetDispatcher.props(maker))
      override val supervisorStrategy = OneForOneStrategy() {
        case f =>
          testActor ! "fail"
          Stop
      }
      def receive = {
        case msg => realDispatcher forward msg
      }
    }))
    dispatcher ! WatchHashtag("test")
  }

  test("TweetDispatcher spawns a child when sent a valid WatchHashtag message") {
    new CommonTweetDispatcherTest {
      assert(dispatcher.underlyingActor.children("test") === counterProbe.ref)
    }
  }

  test("TweetDispatcher removes a child when sent a valid UnwatchHashtag message") {
    new CommonTweetDispatcherTest {
      dispatcher ! UnwatchHashtag("test")
      assert(dispatcher.underlyingActor.children.get("test") === None)
    }
  }

  test("TweetDispatcher relays tweets to a child when it finds watched hashtags") {
    new CommonTweetDispatcherTest {
      val fakeTweet = mock[Status]
      val fakeHashtag = mock[HashtagEntity]
      (fakeHashtag.getText _).expects().returning("test")
      (fakeTweet.getHashtagEntities _).expects().returning(Array(fakeHashtag))

      dispatcher ! fakeTweet
      counterProbe.expectMsg(fakeTweet)
    }
  }

  test("TweetDispatcher doesn't relay tweets to a child when it doesn't find watched hashtags") {
    new CommonTweetDispatcherTest {
      val fakeTweet = mock[Status]
      (fakeTweet.getHashtagEntities _).expects().returning(Array())

      dispatcher ! fakeTweet
      counterProbe.expectNoMsg()
    }
  }

  test("TweetDispatcher ignores WatchHashtag messages for hashtags that are already being watched") {
    new ExceptionThrowingTest {
      dispatcher ! WatchHashtag("test")
      expectNoMsg()
    }
  }

  test("TweetDispatcher ignores UnwatchHashtag messages for hashtags that aren't being watched") {
    new CommonTweetDispatcherTest {
      val childrenBefore = dispatcher.underlyingActor.children
      dispatcher ! UnwatchHashtag("doesn't exist")
      assert(childrenBefore === dispatcher.underlyingActor.children)
    }
  }

}
