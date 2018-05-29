package com.kgribov.telegram.bot

import com.kgribov.telegram.bot.loader.MetaInfoLoader
import com.kgribov.telegram.bot.model.CommandMessage


package object security {

  trait ChatPermission {
    def isAllowed(message: CommandMessage, metaInfoLoader: MetaInfoLoader): Boolean

    def permissionsMessage(notAllowedCommand: String, userName: String): String = {
      s"Sorry $userName, but command $notAllowedCommand is not allowed for you"
    }
  }

  class MultiPermissions(permissions: Seq[ChatPermission] = Seq(new GroupsAllowed(), new PrivateChatAllowed()))
    extends ChatPermission {

    override def isAllowed(message: CommandMessage, metaInfoLoader: MetaInfoLoader): Boolean = {
      permissions.exists(_.isAllowed(message, metaInfoLoader))
    }
  }

  class GroupsAllowed(userType: UserType = ANYONE, onlyForGroups: Seq[Long] = Seq()) extends ChatPermission {
    override def isAllowed(message: CommandMessage, metaInfoLoader: MetaInfoLoader): Boolean = {
      val chatId = message.chat.id
      if (!isGroupChat(message) && isOnlyForGroups(chatId, onlyForGroups)) {
        false
      } else {
        if (userType == ADMIN_ONLY) {
          val admins = metaInfoLoader.loadAdministrators(chatId)
          admins.map(_.id).contains(message.from.id) && isOnlyForGroups(chatId, onlyForGroups)
        } else {
          isOnlyForGroups(chatId, onlyForGroups)
        }
      }
    }

    private def isOnlyForGroups(chatId: String, onlyForGroups: Seq[Long]): Boolean = {
      onlyForGroups.contains(chatId) || onlyForGroups.isEmpty
    }

    private def isGroupChat(message: CommandMessage): Boolean = {
      message.chat.chatType == "group" || message.chat.chatType == "supergroup"
    }
  }

  class PrivateChatAllowed(onlyForUsers: Seq[Long] = Seq()) extends ChatPermission {
    override def isAllowed(message: CommandMessage, metaInfoLoader: MetaInfoLoader): Boolean = {
      if (isPrivateChat(message) && onlyForUsers.isEmpty) {
        true
      } else {
        isPrivateChat(message) && onlyForUsers.contains(message.from.id)
      }
    }

    private def isPrivateChat(message: CommandMessage): Boolean = {
      message.chat.chatType == "private"
    }
  }

  type UserType = String
  val ANYONE: UserType = "ANYONE"
  val ADMIN_ONLY: UserType = "ADMIN_ONLY"
}
