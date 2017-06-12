import React from "react";
import Modal from "react-bootstrap/lib/Modal";
import Button from "react-bootstrap/lib/Button";
import ButtonGroup from "react-bootstrap/lib/ButtonGroup";
import client from "./restClient";
import stompClient from "./libs/websocket/websocket-listener";

class LoadPopup extends React.Component {

    constructor(props) {
        super(props);

        this.state = {showModal: false};
    }

    close = () => {
        this.setState({showModal: false});
    }

    open = () => {
        this.setState({showModal: true});
    }

    updateStart = () => {
        client({method: 'POST', path: '/updateLibrary/STARTED'}).done();
    }

    updatePause = () => {
        client({method: 'POST', path: '/updateLibrary/PAUSED'}).done();
    }

    updateStop = () => {
        client({method: 'POST', path: '/updateLibrary/STOPPED'}).done();
    }

    componentDidMount() {
        stompClient.register([
            {route: '/topic/info', callback: this.refreshMsgInfo}
        ]);
    }

    refreshMsgInfo = (message) => {
        console.log(message);
    }

    render() {
        return (
            <Modal show={this.state.showModal} onHide={this.close}>
                <Modal.Header closeButton>
                    <Modal.Title>Update library</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <ButtonGroup>
                        <Button onClick={this.updateStart}>Update start</Button>
                        <Button onClick={this.updatePause}>Update pause</Button>
                        <Button onClick={this.updateStop}>Update stop</Button>
                    </ButtonGroup>
                </Modal.Body>
                <Modal.Footer>
                    <Button onClick={this.close}>Close</Button>
                </Modal.Footer>
            </Modal>
        );
    }
}

export default LoadPopup;