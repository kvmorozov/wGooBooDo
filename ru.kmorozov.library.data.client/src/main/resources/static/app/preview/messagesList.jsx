import React from "react";
import stompClient from "./../libs/websocket/websocket-listener";

class MessagesList extends React.Component {

    componentDidMount() {
        stompClient.register([
            {route: '/topic/info', callback: this.refreshMsgInfo}
        ]);
    }

    refreshMsgInfo = (message) => {
        this.setState({msg: message.body});
    }

    render() {
        let msg = this.state == null ? "No messages" : this.state.msg;

        return (
            <div>
                {msg}
            </div>
        )
    }

}

export default MessagesList;