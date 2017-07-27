import React from "react";
import Modal from "react-bootstrap/lib/Modal";
import Button from "react-bootstrap/lib/Button";
import ButtonGroup from "react-bootstrap/lib/ButtonGroup";
import client from "../restClient";

class FindDuplicates extends React.Component {

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

    findDuplicates = () => {
        client({method: 'GET', path: '/findDuplicates'}).done();
    }

    render() {
        return (
            <Modal show={this.state.showModal} onHide={this.close}>
                <Modal.Header closeButton>
                    <Modal.Title>Find Duplicates</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <ButtonGroup>
                        <Button onClick={this.findDuplicates}>Find</Button>
                    </ButtonGroup>
                </Modal.Body>
                <Modal.Footer>
                    <Button onClick={this.close}>Close</Button>
                </Modal.Footer>
            </Modal>
        );
    }
}

export default FindDuplicates;