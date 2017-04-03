import React from "react";
import Modal from "react-bootstrap/lib/Modal";
import Button from "react-bootstrap/lib/Button";
import client from "./restClient";

class LoadPopup extends React.Component {

    constructor(props) {
        super(props);

        this.state = {showModal: false};

        this.close = this.close.bind(this);
        this.open = this.open.bind(this);
        this.update = this.update.bind(this);
    }

    close() {
        this.setState({showModal: false});
    }

    open() {
        this.setState({showModal: true});
    }

    update() {
        client({
            method: 'POST', path: '/updateLibrary'
        }).done(
        );
    }

    render() {
        return (
            <div>
                <Modal show={this.state.showModal} onHide={this.close}>
                    <Modal.Header closeButton>
                        <Modal.Title>Update library</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <Button onClick={this.update}>Update</Button>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.close}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        );
    }
}

export default LoadPopup;