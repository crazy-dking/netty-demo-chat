package cn.itcast.server.handler;

import cn.itcast.message.GroupCreateRequestMessage;
import cn.itcast.message.GroupCreateResponseMessage;
import cn.itcast.server.session.Group;
import cn.itcast.server.session.GroupSessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.List;
import java.util.Set;

/**
 * @Description:
 * 创建群组：判断是否有当前群。
 * 将
 * @author: zjdking
 * @date: 2021/12/30 22:45
 */
@ChannelHandler.Sharable
public class GroupCreateRequestMessageHandler extends SimpleChannelInboundHandler<GroupCreateRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, GroupCreateRequestMessage msg) throws Exception {
        // 获得要创建的群聊名
        String groupName = msg.getGroupName();
        // 获得要创建的群聊的成员组
        Set<String> members = msg.getMembers();
        // 判断该群聊是否创建过，未创建返回null并创建群聊
        Group group = GroupSessionFactory.getGroupSession().createGroup(groupName, members);
        if (group == null) {
            // 发送创建成功消息
            GroupCreateResponseMessage groupCreateResponseMessage = new GroupCreateResponseMessage(true, groupName + "创建成功");
            ctx.writeAndFlush(groupCreateResponseMessage);
            // 获得在线群员的channel，给群员发送入群聊消息
            List<Channel> membersChannel = GroupSessionFactory.getGroupSession().getMembersChannel(groupName);
            groupCreateResponseMessage = new GroupCreateResponseMessage(true, "您已被拉入"+groupName);
            // 给每个在线群员发送消息
            for(Channel channel : membersChannel) {
                channel.writeAndFlush(groupCreateResponseMessage);
            }
        } else {
            // 发送失败消息
            GroupCreateResponseMessage groupCreateResponseMessage = new GroupCreateResponseMessage(false, groupName + "已存在");
            ctx.writeAndFlush(groupCreateResponseMessage);
        }

    }
}
